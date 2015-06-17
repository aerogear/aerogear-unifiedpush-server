#!/bin/bash

install_files() {
	local base_dir="$1"

	export SOURCE_DIR="$(get_full_path $(dirname "$0"))"
	
	install_recursive "../../../unifiedpush" "${U_SERVER_SHARE}"	
	install_recursive "../../../wildfly" "${U_SERVER_SHARE}"
}

main() {
	install_files
}

install_recursive() {
    local src="$1"
    local dst="$2"
    local d
        
    install -m 0755 -d "${DESTDIR}${dst}"
    ( cd "${SOURCE_DIR}/${src}" && find . -type d ) | while read d; do
        if [ "${d}" != "." ]; then
            [ -d "${DESTDIR}${dst}/${d}" ] || install -m 0755 -d "${DESTDIR}${dst}/${d}" || die "Cannot create ${d}"
        fi
    done || die "Cannot copy recursive ${src}"
    
    ( cd "${SOURCE_DIR}/${src}" && find . -type f ) | while read f; do
        local mask
        [ "${f%%.sh}" == "${f}" ] && mask="0644" || mask="0755"
        [ "${f%%.war}" == "${f}" ] || mask="0755"
        install -m "${mask}" "${SOURCE_DIR}/${src}/${f}" "${DESTDIR}${dst}/${f}" || die "Cannot install ${f}"
    done || die "Cannot copy recursive ${src}"
    
    return 0
}

U_UNIFIEDPUSH_SERVER_HOME="${U_UNIFIEDPUSH_SERVER_HOME:-server}"
U_SERVER_SHARE="/usr/share/unifiedpush/${U_UNIFIEDPUSH_SERVER_HOME}"

main

exit 0
