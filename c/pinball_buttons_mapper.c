/*
 * PinballButtons Android project.
 * (C) 2013 by Pascal Bauermeister
 *
 * pinball_buttons_mapper.c - Keyboard-to-touchscreen event mapper.
 *
 * Reads events from the keyboard device, and generates touchscreen
 * events.
 *
 * The parameters are passed as a config file (which is monitored for
 * changed, and reloaded there upon).  A typical config file looks
 * like this: 
 *   device_rotation 270
 *   kb_device /dev/input/event4
 *   ts_device /dev/input/event2
 *   screen_height 976
 *   screen_width 600
 *   ts_type A
 *
 * This program and the config files are meant to be started and
 * created by the Android Java application.
 *
 * Useful resources:
 * - command to list devices: adb shell cat /proc/bus/input/devices
 * - info about Linux kernel support for multitouch events:
 *   http://www.mjmwired.net/kernel/Documentation/input/multi-touch-protocol.txt
 *
 * Please visit http://ten.homelinux.net/pb/pinball/ for more info.
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/inotify.h>
#include <linux/input.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>

#define ME            "pinball_buttons_mapper"
#define PERROR(x ...) { fprintf(stderr, ME ": " x); fputs("\n", stderr); }
#define LOG(x ...)    { printf(ME ": " x); puts(""); fflush(stdout); }
#define LOGN(x ...)    { printf(ME ": " x); fflush(stdout); }

extern void daemonize(const char* argv0); /* from daemon.c */

typedef int bool;

/* This stores our idea of the shift keys states */
typedef struct {
  char lshift;
  char rshift;
  char changed;
} KeyStates;

/* This stores one TS coordinate */
typedef struct {
  long x;
  long y;
} TsPoint;

/*
 * This stores all our parameters, most of them are red from the
 * config file
 */
typedef struct {
  /* from config file */
  const char* ts_device;
  const char* kb_device;
  int device_rotation;
  int screen_height;
  int screen_width;
  char ts_type;
  /* others */
  int margin_h;
  int margin_v;
  int status_bar_size;
  const char* config_file;
  time_t config_mtime;
  int lbx, lby, rbx, rby; /* left/right buttons ts event coords */
} Params;

/*
 * Send an event to an open device
 */
void send(int fd, __u16 type, __u16 code, __s32 value)
{
  struct input_event ev;
  size_t n;

  LOG(">>> [%d] %04x, %04x, %08x", sizeof(ev), type, code, value);

  memset(&ev, 0, sizeof(ev));
  ev.type = type;
  ev.code = code;
  ev.value = value;

  n = write(fd, &ev, sizeof(ev));
}

/*
 * Waits for input on file descriptor, or timeout.
 * Returns what select() returns.
 */
int input_timeout(int filedes, unsigned int seconds)
{
  fd_set set;
  struct timeval timeout;
  
  /* Initialize the file descriptor set. */
  FD_ZERO(&set);
  FD_SET(filedes, &set);
  
  /* Initialize the timeout data structure. */
  timeout.tv_sec = seconds;
  timeout.tv_usec = 0;
  
  /* select returns 0 if timeout, 1 if input available, -1 if error. */
  return select(FD_SETSIZE, &set, NULL, NULL, &timeout);
}

/*
 * Gets modification time of a file.
 */
time_t get_mtime(const char* name) {
  FILE* f = fopen(name, "r");
  struct stat stat;
  fstat(fileno(f), &stat);
  fclose(f);
  return stat.st_mtime;
}

void send_events_a(int fd, bool lshift, bool rshift, Params *params)
{
  if (!lshift && !lshift) {
    send(fd, EV_SYN, SYN_MT_REPORT, 0);
  }
  if (lshift) {
    send(fd, EV_ABS, ABS_MT_TRACKING_ID, 1);
    send(fd, EV_ABS, ABS_MT_POSITION_X, params->lbx);
    send(fd, EV_ABS, ABS_MT_POSITION_Y, params->lby);
    send(fd, EV_SYN, SYN_MT_REPORT, 0);
  }
  if (rshift) {
    send(fd, EV_ABS, ABS_MT_TRACKING_ID, 2);
    send(fd, EV_ABS, ABS_MT_POSITION_X, params->rbx);
    send(fd, EV_ABS, ABS_MT_POSITION_Y, params->rby);
    send(fd, EV_SYN, SYN_MT_REPORT, 0);
  }
  send(fd, EV_SYN, SYN_REPORT, 0);
}

void send_events_b(int fd, bool lshift, bool rshift, Params *params)
{
  if (lshift) {
    send(fd, EV_ABS, ABS_MT_SLOT, 1);
    send(fd, EV_ABS, ABS_MT_TRACKING_ID, 1);
    send(fd, EV_ABS, ABS_MT_POSITION_X, params->lbx);
    send(fd, EV_ABS, ABS_MT_POSITION_Y, params->lby);
    send(fd, EV_SYN, SYN_REPORT, 0);
  } else {
    send(fd, EV_ABS, ABS_MT_SLOT, 1);
    send(fd, EV_ABS, ABS_MT_TRACKING_ID, -1);
    send(fd, EV_SYN, SYN_REPORT, 0);
  }
  if (rshift) {
    send(fd, EV_ABS, ABS_MT_SLOT, 2);
    send(fd, EV_ABS, ABS_MT_TRACKING_ID, 2);
    send(fd, EV_ABS, ABS_MT_POSITION_X, params->rbx);
    send(fd, EV_ABS, ABS_MT_POSITION_Y, params->rby);
    send(fd, EV_SYN, SYN_REPORT, 0);
  } else {
    send(fd, EV_ABS, ABS_MT_SLOT, 2);
    send(fd, EV_ABS, ABS_MT_TRACKING_ID, -1);
    send(fd, EV_SYN, SYN_REPORT, 0);
  }
}



/*
 * Main loop.
 */
void run(Params *params)
{
  int kb_fd, ts_fd;
  KeyStates key_states = { 0, 0, 0 };
  struct input_event event;
  bool is_type_a = params->ts_type == 'A';

  LOG("Opening ts device %s ...", params->ts_device);
  ts_fd = open(params->ts_device, O_WRONLY);
  if (ts_fd < 0) {
    PERROR("Could not open ts device.");
    return;
  }

  /* try to open device or wait awhile */
  LOG("Opening kbd device %s ...", params->kb_device);
  kb_fd = open(params->kb_device, O_RDONLY);
  if (kb_fd < 0) {
    PERROR("Cannot open kbd device. Will retry.");
    close(ts_fd);
    return;
  }

  while (1) {
    int res;

    res = input_timeout(kb_fd, 1);

    if (res==0) {
      /* timeout */
      time_t mtime = get_mtime(params->config_file);
      if (mtime != params->config_mtime) {
	LOG("Config file changed: %ld %ld", mtime, params->config_mtime);
	return;
      }
      continue;
    }
    else if (res<0) {
      PERROR("Select error");
      return;
    }

    if ((res = read(kb_fd, &event, sizeof(event))) < 0) { 
      LOG("-> %d", res);
      break;
    }
    /* Filter the events that we are interested in. See
     * http://www.kernel.org/doc/Documentation/input/event-codes.txt */
    
    /* LOG("=> event %04x %04x %08x", event.type, event.code, event.value); */

    key_states.changed = 0;
    if (event.type == EV_KEY && event.value <= 1) {
      LOG(" > EV_KEY %04x %08x", event.code, event.value);
      if (event.code == KEY_LEFTSHIFT) {
	LOG(" > LSHIFT");
	key_states.lshift = event.value;
	key_states.changed = 1;
      }
      else if (event.code == KEY_RIGHTSHIFT) {
	LOG(" > RSHIFT");
	key_states.rshift = event.value;
	key_states.changed = 1;
      }
    }

    /* If something significant happened, handle it */
    if (key_states.changed) {
      if (is_type_a)
	send_events_a(ts_fd, key_states.lshift, key_states.rshift, params);
      else
	send_events_b(ts_fd, key_states.lshift, key_states.rshift, params);
    }	

  } /* while(read()) */

  close(kb_fd);
  close(ts_fd);
}

/*
 * Parses a line in the form
 *   KEY VALUE
 */
void parseLine(char* line, char** key, char** value) {
  const char* sep = " \n";
  *key = strtok(line, sep);
  *value = strtok(NULL, sep);
}

/*
 * Loads the config file into a Params struct.
 */
int load_config(const char* config_file, Params *p)
{
  FILE *f;
  char line[1000];

  f = fopen(config_file, "r");
  if (!f) {
    PERROR("Cannot open log file: '%s'", config_file);
    sleep(4);
    return 0;
  }
  p->config_file = config_file;
  p->config_mtime = get_mtime(p->config_file);

  /* config file parsing */
  while ( fgets(line, sizeof line, f) != NULL ) {
    char *key, *value;

    LOGN("Cnf line: %s", line);
    parseLine(line, &key, &value);
    LOG("  parsed: [%s] = [%s]", key, value);
    if(!key || !value) {
      LOG("  skipping");
      continue;
    }

    if (!strcmp("device_rotation", key))
      p->device_rotation = atoi(value);
    else if (!strcmp("kb_device", key))
      p->kb_device = strdup(value);
    else if (!strcmp("ts_device", key))
      p->ts_device = strdup(value);
    if (!strcmp("screen_height", key))
      p->screen_height = atoi(value);
    if (!strcmp("screen_width", key))
      p->screen_width = atoi(value);
    if (!strcmp("ts_type", key))
      p->ts_type = value[0];
  }
  fclose(f);

  /* compute event coords */
  switch (p->device_rotation) {
  case 270:
    p->lbx = p->screen_height   - p->margin_v;
    p->lby = p->screen_width    - p->margin_h;
    p->rbx = p->screen_height   - p->margin_v;
    p->rby =                      p->margin_h;
    break;

  case 180:
    p->rbx =                      p->margin_h;
    p->rby = p->status_bar_size + p->margin_v;
    p->lbx = p->screen_width    - p->margin_h;
    p->lby = p->status_bar_size + p->margin_v;
    break;

  case 90:
    p->rbx = p->status_bar_size + p->margin_v;
    p->rby = p->screen_width    - p->margin_h;
    p->lbx = p->status_bar_size + p->margin_v;
    p->lby =                      p->margin_h;
    break;

  case 0:
  default:
    p->lbx =                      p->margin_h;
    p->lby = p->screen_height   - p->margin_v;
    p->rbx = p->screen_width    - p->margin_h;
    p->rby = p->screen_height   - p->margin_v;
    break;
  }

  LOG("params.ts_device       = [%s]", p->ts_device);
  LOG("params.kb_device       = [%s]", p->kb_device);
  LOG("params.device_rotation = %d", p->device_rotation);
  LOG("params.screen_height   = %d", p->screen_height);
  LOG("params.screen_width    = %d", p->screen_width);
  LOG("params.ts_type         = %c", p->ts_type);
  LOG("params.margin_h        = %d", p->margin_h);
  LOG("params.margin_v        = %d", p->margin_v);
  LOG("params.config_mtime    = %ld", p->config_mtime);
  LOG("params.lbx,lby rbx,rby = %d,%d %d,%d",
      p->lbx, p->lby,
      p->rbx, p->rby);

  return 1;
}

/*
 * Main function.
 */
int main(int argc, char **argv)
{
  const char *config_file = "/data/data/net.homelinux.ten.pinballbuttons/files/apps_settings.cnf";
  Params params = {
    "/dev/input/event2",
    "/dev/input/event4",
    270,
    976,
    600,
    'A',
    80,
    50,
    48,
    NULL,
    0,
  };
  struct stat stat;
  int opt = 0;
  bool sticky = 0;

  /* parse arguments*/
  while ((opt = getopt(argc, argv, "sh")) != -1) {
    switch(opt) {
    case 's':
      sticky = 1;
      break;
    case 'h':
      printf("Usage: " ME " [-s] [CONFIG_FILE]\n"
	     "Keyboard-to-touchscreen event mapper for the PinballButtons "
	     "Android app\n"
	     "\n"
	     "  -s   sticky; do not start as daemon, print debug info\n"
	     "  -h   display this help and exit\n"
	     "\n"
	     "CONFIG_FILE contains the configuration; if omitted, try "
	     "to use the file\n"
	     "%s.\n"
	     "\n"
	     "Please visit http://ten.homelinux.net/pb/pinball/ .\n",
	     config_file);
      exit(0);
    }
  }
  if (optind < argc) {
    config_file = argv[optind];
  }

  if (!sticky) {
    LOG("Starting as daemon.");
    daemonize(argv[0]);
  }

  LOG("Starting");
  while (1) {
    if (load_config(config_file, &params))
      run(&params);
    sleep(1);
  }

  LOG("Ended");
}
