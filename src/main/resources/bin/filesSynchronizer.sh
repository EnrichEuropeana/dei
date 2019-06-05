#!/usr/bin/env bash

readonly localUser="user"
readonly remoteLocation="location"
readonly localDir="/home/$localUser/files/out/"
readonly logFile="/home/$localUser/bin/synchronization.log"

#1. Check if 'lock_synchro' file exists in specified directory
if [ -e /home/$localUser/bin/lock_synchro ]
then
    echo "`date '+%Y-%m-%d %H:%M:%S'` Synchronization is locked. Will not be run" >> $logFile
    exit 1
else
    echo "`date '+%Y-%m-%d %H:%M:%S'` Starting synchronization" >> $logFile
    touch /home/$localUser/bin/lock_synchro
    rsync -a --progress --remove-source-files $localDir $remoteLocation >> $logFile
    echo "`date '+%Y-%m-%d %H:%M:%S'` Synchronization finished" >> $logFile
    rm /home/$localUser/bin/lock_synchro
fi
