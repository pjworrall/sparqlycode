# ~/.profile: executed by the command interpreter for login shells.
# This file is not read by bash(1), if ~/.bash_profile or ~/.bash_login
# exists.
# see /usr/share/doc/bash/examples/startup-files for examples.
# the files are located in the bash-doc package.

# the default umask is set in /etc/profile; for setting the umask
# for ssh logins, install and configure the libpam-umask package.
#umask 022

# if running bash
if [ -n "$BASH_VERSION" ]; then
    # include .bashrc if it exists
    if [ -f "$HOME/.bashrc" ]; then
	. "$HOME/.bashrc"
    fi
fi

# set PATH so it includes user's private bin if it exists
if [ -d "$HOME/bin" ] ; then
    PATH="$HOME/bin:$PATH"
fi

# PJW 6/5/2012
export PATH=$HOME/jdk1.6.0_31/bin:$PATH
export JAVA_HOME=$HOME/jdk1.6.0_31
export TOMCAT_HOME=$HOME/apache-tomcat-7.0.27

# PJW 20/03/104
export PATH=$PATH:$HOME/sparqlycode/bin
