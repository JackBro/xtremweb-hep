#
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


SHELL=/bin/sh

all: install

distrib: %
	./build.sh $@ 
rpmserver: %
	./build.sh -f install.xml $@ 
rpmworker: %
	./build.sh -f install.xml $@ 
install: distrib
	./build.sh -f install.xml $@ 
createKeys: %
	./build.sh -f install.xml $@ 
sonar-clean: %
	./build.sh -f build.xml $@ 
sonar: %
	./build.sh -f build.xml $@ 
junit: %
	./build.sh -f build.xml $@ 
coverage: %
	./build.sh -f build.xml $@ 
doc: %
	./build.sh -f build.xml $@ 
doxygen: %
	./build.sh -f build.xml $@ 
installDB: %
	./build.sh -f install.xml $@ 
removeDB: %
	./build.sh -f install.xml $@ 
uninstall: %
	./build.sh -f install.xml $@ 
uninstallAll: %
	./build.sh -f install.xml $@ 
clean:
	./build.sh -f build.xml $@ 
%:
	@if test ! -f "./build.conf" ; then echo "  **** oups!! Where is your 'build.conf' ?!? **** " ; echo ; exit 2 ; fi
	@if grep "\*\*\* MUST BE" build.conf > /dev/null 2>&1 ; then  echo "  *****" ; echo "  *****" ; echo "  ***** ERROR: Your build.conf file is not filled properly" ; echo "  *****" ; echo "  *****" ; echo ; exit 3 ; else echo " build.conf OK" ; fi
	@echo
