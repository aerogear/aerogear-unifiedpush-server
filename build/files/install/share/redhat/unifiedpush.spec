#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
%define _topdir      ${basedir}/target	 
%define buildroot    %{_topdir}/%{name}-%{version}-root
%define _sourcedir   %{_topdir}
%define __jar_repack 0
%define _binaries_in_noarch_packages_terminate_build   0

Summary:	Unifiedpush Server. 
Name:		unifiedpush
Version:	${rpm.version}
Release:	${buildNumber}
License:	Apache License, Version 2.0
Group:		Applications/Servers
Source:		unifiedpush-server-${project.version}.tar.gz
Packager:	Yaniv Marom-Nachumi
Vendor:		Unifiedpush
URL:		Unifiedpush
BuildRoot:	%{buildroot}
BuildArch:	noarch
Requires:   logrotate, crontabs, java-1.7.0-openjdk, httpd >= 2.2, postgresql-server >= 9.3

%description
UnifiedPush Server is server sending native push messages to different mobile operating systems, 
such as Android, iOS, Windows or Firefox OS/SimplePush.
 
%prep
rm -rf "${RPM_BUILD_ROOT}"
%setup -n unifiedpush-${project.version}

%install
rm -fr "${RPM_BUILD_ROOT}"
DESTDIR="${RPM_BUILD_ROOT}" \
	./install/bin/redhat/install.sh

%clean
rm -rf "${RPM_BUILD_ROOT}"

%pre
%{_sbindir}/groupadd -r unifiedpush 2> /dev/null || :
%{_sbindir}/useradd -c "Unifiedpush Server User" -g unifiedpush \
    -s /sbin/nologin -r -d / unifiedpush 2> /dev/null || :
     
%files
%defattr(-,root,root)
/usr

%changelog
