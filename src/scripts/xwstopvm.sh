#!/bin/sh

# Copyrights     : CNRS
# Author         : Oleg Lodygensky
# Acknowledgment : XtremWeb-HEP is based on XtremWeb 1.8.0 by inria : http://www.xtremweb.net/
# Web            : http://www.xtremweb-hep.org
# 
#      This file is part of XtremWeb-HEP.
#
#    XtremWeb-HEP is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    XtremWeb-HEP is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with XtremWeb-HEP.  If not, see <http://www.gnu.org/licenses/>.
#
#



#  ******************************************************************
#  File    : xwstopvm.sh
#  Date    : November, 2011
#  Author  : Oleg Lodygensky
# 
#  OS      : Linux
# 
#  Purpose : this script stops a VirtualBox VM on worker side
#
# One env variables must be set when calling this script : 
# (it is automatically set by the worker)
#  - XWJOBUID : this must contains the directory where drive are stored
#
# This script does not unregister the VM or its VDI
# Unregistering is done by xwstartvm.sh::clean()
# 
#  !!!!!!!!!!!!!!!!    DO NOT EDIT    !!!!!!!!!!!!!!!!
#  Remarks : this script is auto generated by install process
#  ******************************************************************



##################################################
# some variables
##################################################


ROOTDIR=`dirname $0`


VBROOT=/Applications/VirtualBox.app/Contents/MacOS/
VBMGT=$VBROOT/VBoxManage
VBHL=$VBROOT/VBoxHeadLess
#VBHL=$VBROOT/VirtualBox


THISOS=`uname -s`

case $THISOS in
    "Darwin" )
		VBROOT=/Applications/VirtualBox.app/Contents/MacOS/
		VBMGT=$VBROOT/VBoxManage
		VBHL=$VBROOT/VBoxHeadLess
     ;;
    "Linux" )
		VBROOT=/usr/bin
		VBMGT=$VBROOT/vboxmanage
		VBHL=$VBROOT/vboxheadless
	;;
    * )
		fatal "$THISOS not supported"
	;;
esac



##################################################
# clean()
##################################################
clean() {

    [ "X$VMNAME" = "X" ] && return

    $VBMGT metrics list $VMNAME > /dev/null 2>&1
    [ $? -ne 0 ] && return

    echo "[`date`] ($0) : clean"

	$VBMGT controlvm $VMNAME poweroff

#
# let wait until the VM is effectively unregistered by xwstartvm.sh::clean()
#	
	for (( i=0 ; i<10; i++ )) ; do
	    $VBMGT metrics list $VMNAME > /dev/null 2>&1
    	[ $? -ne 0 ] && break
    	sleep 10
	done
}


##################################################
# fatal()
##################################################
fatal() {

	msg=$1
	[ "X$msg" = "X" ] && msg="Ctrl+C"
#	sleep $WAITDELAY
	
	echo "[`date`] ($0) FATAL : $msg"

    clean

    exit 1
}



##################################################
# main
##################################################

trap fatal SIGINT SIGTERM

echo ""
echo "[`date`] ($0) : start"

if [ "Z$XWJOBUID" = "Z" ] ; then
    fatal "XWJOBUID is not set"
fi

VMNAME=xwvm$XWJOBUID

clean


###########################################################
#     EOF        EOF     EOF        EOF     EOF       EOF #
###########################################################
