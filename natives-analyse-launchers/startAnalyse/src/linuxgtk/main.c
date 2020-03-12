#include <stdlib.h>
#include <stdio.h>
#include <sys/stat.h>
#include <gtk/gtk.h>
#include <unistd.h>
#include <string.h>

const int RESTART_ANALYSE = 1;

static void launchBrowser (GtkWidget *wid, GtkWidget *win)
{
  system("firefox http://www.java.com/fr/download/manual.jsp");
}

static void quit (GtkWidget *wid, GtkWidget *win)
{
  gtk_main_quit();
}

int main (int argc, char *argv[])
{

  pid_t pid = getpid();
  char *pidStr = (char *)calloc(PATH_MAX, sizeof(char));
  char *pidNumberStr = (char *)calloc(PATH_MAX, sizeof(char));
  char *execCmd = (char *)calloc(PATH_MAX, sizeof(char));

  strcat(pidStr, "readlink /proc/");
  sprintf (pidNumberStr, "%d", pid);
  strcat(pidStr, pidNumberStr);
  strcat(pidStr, "/exe");
  strcat(pidStr, " > executablePath.txt");

  system(pidStr);

  FILE *file = fopen("./executablePath.txt", "r");
  if (file == NULL )             /* Could not open file */
  {
    printf("Error opening 'executablePath.txt' file !!!");
    return 1;
  }
  fscanf(file, "%s", execCmd);
  fclose(file);

  char *p = strrchr(execCmd, '/');
  printf("%d",p - execCmd);
  sprintf(execCmd, "%.*s", p - execCmd , execCmd);
  strcat(execCmd,"/LINUXAnalyseLauncher.sh");


  printf("str3: %s\n", execCmd);

  system("rm executablePath.txt");

  system("which java > checkJavaDirectory.txt");
  int javaExist = 0;

  struct stat stat_result;
  char filename[]= "checkJavaDirectory.txt";
  if(stat(filename, &stat_result) == -1) return 1;
  if(stat_result.st_size > 0) javaExist = 1;
  system("rm checkJavaDirectory.txt");

  if(javaExist) {
      execl("/bin/sh","sh","-c",execCmd,NULL);
      /*int status = system(execCmd);
      status = status>>8;
      printf("return code : %d\n", status);
      if(status == RESTART_ANALYSE) {
        printf("ReLaunch analyse\n");
        system("./startAnalyse");
      }*/
      printf("exit\n");
      return 0;
  }
  GtkWidget *window = NULL;
  GtkWidget *container = NULL;
  GtkWidget *label1 = NULL;
  GtkWidget *label2 = NULL;
  GtkWidget *button = NULL;
  GtkWidget *button2 = NULL;
  GdkColor color;

  /* Initialize GTK+ */
  g_log_set_handler ("Gtk", G_LOG_LEVEL_WARNING, (GLogFunc) gtk_false, NULL);
  gtk_init (&argc, &argv);
  g_log_set_handler ("Gtk", G_LOG_LEVEL_WARNING, g_log_default_handler, NULL);

  /* Create the main window */
  window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  gtk_container_set_border_width (GTK_CONTAINER (window ), 8);
  gtk_window_set_title (GTK_WINDOW (window ), "Linux Analyse Launcher");
  gtk_window_set_position (GTK_WINDOW (window), GTK_WIN_POS_CENTER);
  //gtk_window_set_icon_from_file(GTK_WINDOW (window), "analyse2.gif", NULL);
  gtk_widget_realize (window);
  g_signal_connect (GTK_WINDOW(window), "destroy", gtk_main_quit, NULL);


  /* Create a vertical container with labels & button */
  container = gtk_vbox_new (TRUE, 6);
  gtk_container_add (GTK_CONTAINER (window), container);

  label1 = gtk_label_new("You must have a Java Runtime Environment installed");
  gtk_box_pack_start (GTK_BOX (container), label1, TRUE, TRUE, 0);

  label2 = gtk_label_new("in order to run Analyse. Please visit Sun's web Site.");
  gtk_box_pack_start (GTK_BOX (container), label2, TRUE, TRUE, 0);

  gdk_color_parse ("red", &color);
  gtk_widget_modify_fg(GTK_WIDGET(label1),GTK_STATE_NORMAL,&color);
  gtk_widget_modify_fg(GTK_WIDGET(label2),GTK_STATE_NORMAL,&color);

  button = gtk_button_new_with_label ("http://www.java.com/fr/download/manual.jsp");
  g_signal_connect (G_OBJECT (button), "clicked", G_CALLBACK (launchBrowser), (gpointer) window);
  gtk_box_pack_start (GTK_BOX (container), button, TRUE, TRUE, 0);

  button2 = gtk_button_new_with_label ("Quit");
  g_signal_connect (G_OBJECT(button2), "clicked", G_CALLBACK (quit), (gpointer) window);
  gtk_box_pack_start (GTK_BOX (container), button2, TRUE, TRUE, 0);

  /* Enter the main loop */
  gtk_widget_show_all (window);
  gtk_main ();
  return 0;
}
