#!/bin/sh

get_full_path() {
    # Absolute path to this script, e.g. /home/user/bin/foo.sh
    SCRIPT=$(readlink -f $0)

    if [ ! -d ${SCRIPT} ]; then
        # Absolute path this script is in, thus /home/user/bin
        SCRIPT=`dirname $SCRIPT`
    fi

    ( cd "${SCRIPT}" ; pwd )
}

SCRIPT_PATH="$(get_full_path ./)"

. "${SCRIPT_PATH}/common/common.vars"
. "${SCRIPT_PATH}/server.vars"
. "${SCRIPT_PATH}/server.redhat.vars"

. "${SCRIPT_PATH}/common/common.inc"
. "${SCRIPT_PATH}/common/db-common.inc"
. "${SCRIPT_PATH}/common/pgsql/pgsql-util.inc"
. "${SCRIPT_PATH}/common/pgsql/pgsql-schema-util.inc"

SUBST_VARS="C_COMPONENT C_SERVER_BIN SERVER_PROFILE_INDEX SERVER_PROFILE_IP C_PROFILE_NAME C_PROFILE_TYPE SERVER_ENCODING WILDFLY_PROFILE SYSTEM_CONF SERVER_USER SERVER_RUN SERVER_OPT SERVER_LOGS SERVER_STATE SERVER_CONF DB_HOST DB_PORT DB_NAME"

revert() {
    if [ -z "${PROFILE_EXISTS}" -a "${ACTION}" = "create" ]; then
        print_debug "Reverting last profile command ..."
        _remove_profile 1
    fi
}

main(){
    is_root_user
    verify_database_server
    
    case "${ACTION}" in
        create)
            create_profile
            ;;
        update)
            update_profile
            ;;
        remove)
            remove_profile
            ;;		
    esac
}

create_profile(){
    [ -n "${PROFILE_EXISTS}" ] && die "Server profile already exists."
    
    create_lo_interface
    create_service
    crete_os_config	
    create_wildfly_profile
    
    config_profile	
}

update_profile(){
    print_debug "Update system profile ..."
    
    [ -z "${PROFILE_EXISTS}" ] && die "Profile does not exist"
    
    common_substitute "${SUBST_VARS}" 0 "${C_SERVER_SHARE}/etc/${C_COMPONENT}/server.version.in" "${PROFILE_CONF}/version.properties" || die "${PROFILE_CONF}/version.properties"
    
    # Remove JS Resources
    $("${SCRIPT_PATH}"/setup-js.sh --action=remove --profile=${C_PROFILE_NAME})
    
    # Extract new JS Resources
    $("${SCRIPT_PATH}"/setup-js.sh --action=create --profile=${C_PROFILE_NAME} --type=${C_PROFILE_TYPE})
}

#
# Usage:
#
# Install additional server configuration files
# 
config_profile(){
    print_debug "Create profile configuration ..."
    create_dirs "${SERVER_USER}" "${PROFILE_LOGS}" \
                "${PROFILE_RUN}" \
                "${PROFILE_CONF}" \
                "${PROFILE_STATE}"
    
    config_files
    copy_config_files "${C_SERVER_SHARE}/etc/${C_COMPONENT}" "${PROFILE_CONF}"
    config_webapp
    config_httpd_profile
    
    # Create DB schema
    [ -z "${SKIP_DB}" ] && config_db	
}

#
# Usage:
#
# Copy *.in files and Substitute parameters.  
# 
config_files() {
    print_debug "Substitute configuration files ..."
    
    common_substitute "${SUBST_VARS}" 1 "${C_SERVER_SHARE}/etc/${C_COMPONENT}/db.properties.in" "${PROFILE_CONF}/db.properties" || die "${PROFILE_CONF}/db.properties"
    common_substitute "${SUBST_VARS}" 1 "${C_SERVER_SHARE}/etc/${C_COMPONENT}/logback.xml.in" "${PROFILE_CONF}/logback.xml" || die "${PROFILE_CONF}/logback.xml" 
    common_substitute "${SUBST_VARS}" 1 "${C_SERVER_SHARE}/etc/${C_COMPONENT}/server.version.in" "${PROFILE_CONF}/version.properties" || die "${PROFILE_CONF}/version.properties"
    common_substitute "${SUBST_VARS}" 1 "${C_SERVER_SHARE}/etc/${C_COMPONENT}/environment.properties.in" "${PROFILE_CONF}/environment.properties" || die "${PROFILE_CONF}/environment.properties.in"
      
    
    chown -R "${SERVER_USER}":"${SERVER_USER}" ${PROFILE_STATE}
    chmod -R 775 ${PROFILE_STATE}    
}

config_db(){
    print_debug "Creating Database Schema/User ..."
    case "${DB_TYPE}" in
        pgsql)
            pgsql_schema_create
            ;;
    esac
    
    if [ -z "${SCHEMA_ONLY}" ]; then
        print_debug "Creating Database tables ..."
        "${C_SERVER_BIN}"/_initialize-db-data.sh create || die "Failed on create data (/tmp/maintenance/${C_COMPONENT}.log)"
    fi
    
    if [ -z "${NO_INIT_DATA}" ]; then
        print_debug "Populate default tables with required data ..."
        "${C_SERVER_BIN}"/_initialize-db-data.sh initialize ${C_PROFILE_TYPE} ${SERVER_ENCODING} || die "Failed on initializing data (/tmp/maintenance/${C_COMPONENT}.log)"
    fi
}

is_db_alive() {
    print_debug "Verifying database is alive ..."
    if [ "${DB_TYPE}" = "pgsql" ]; then
        pgsql_is_alive || die "Please start pgsql"
    fi
}

#
# Usage:
#
# Add local interface
# 
create_lo_interface(){
    print_debug "Create loopback network interface ..."
    # Export next available loop-back
    get_server_ip
    
    common_substitute "${SUBST_VARS}" 0 "${C_SERVER_SHARE}/etc/${C_COMPONENT}.lo.conf.in" "${NETWORK_CONF}/ifcfg-lo:${C_COMPONENT}-${C_PROFILE_NAME}" || die "${NETWORK_CONF}/ifcfg-lo:${C_COMPONENT}-${C_PROFILE_NAME}"
    ifup "lo:${SERVER_PROFILE_INDEX}"
    echo "WARNING: Please restart httpd in-order to apply new configuration."
}

#
# Usage:
#
# Add service configuration files
# 
create_service(){
    print_debug "Create init.d service files ..."
    common_substitute "${SUBST_VARS}" 0 "${C_SERVER_SHARE}/etc/${C_COMPONENT}.conf.d.in" "${SYSTEM_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}" || die "${SYSTEM_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}"
    common_substitute "${SUBST_VARS}" 0 "${C_SERVER_SHARE}/etc/${C_COMPONENT}.init.d.redhat.in" "${SYSTEMD_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}" || die "${SYSTEMD_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}"
    
    chmod 755 "${SYSTEM_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}"
    chmod 755 "${SYSTEMD_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}"
}

#
# Usage:
#
# Add os configuration files (cron.d/logrotate.d)
#
crete_os_config() {
    common_substitute "${SUBST_VARS}" 0 "${C_SERVER_SHARE}/etc/cron.d/${C_COMPONENT}.cron.d.redhat.in" "${CROND_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}" || die "${CROND_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}"
}

is_service_running() {
    local server_is_up
    
    common_service_status "${C_COMPONENT}-${C_PROFILE_NAME}" > /dev/null 2>&1 && server_is_up=1
    
    if [ -n "${server_is_up}" ]; then
        [ -n "${server_is_up}" ] && die "Please stop ${C_COMPONENT}-${C_PROFILE_NAME} service."
    fi
}


remove_profile() {
    [ -z "${PROFILE_EXISTS}" ] && die "Profile does not exist"
    _remove_profile "${DO_DESTRUCTIVE}"
}

#
# Usage:
#
# Delete application server profile 
# 
_remove_profile(){
    local do_destructive="$1"
    
    is_profile_exists
    is_service_running
    
    remove_local_interface ${C_COMPONENT}-${C_PROFILE_NAME}
    remove_service_conf
    remove_os_config
    remove_httpd_profile
    
    # Remove OS resources   
    rm -rf "${PROFILE_OPT}"
    
    if [ "${do_destructive}" = 1 ]; then
        [ -z "${SKIP_DB}" ] && is_db_alive && parse_db_properties "${PROFILE_CONF}/db.properties" 

        if [ "${DB_TYPE}" = "pgsql" -a -z "${NO_DB_ROLLBACK}" -a -z "${SKIP_DB}" ]; then
            print_debug "Removing postgres schema ..."
            pgsql_schema_drop
        fi 
        
        rm -rf "${PROFILE_LOGS}"
        rm -rf "${PROFILE_RUN}"
        rm -rf "${PROFILE_CONF}"
        rm -rf "${PROFILE_STATE}"
    fi
}

# Check if server profile exists
is_profile_exists(){
    [ -d "${PROFILE_OPT}" ] || die "Server profile doesn't exists ${PROFILE_OPT}" 
}

#
# Usage:
#
# remove local interface
# 
remove_local_interface(){   
    rm -rf "${NETWORK_CONF}/ifcfg-lo:$1" || die "Unable to find ${NETWORK_CONF}/ifcfg-lo:$1"
    
    echo "WARNING: Please restart network service in-order to apply interface removal!"
}

#
# Usage:
#
# Remove service configuration files
# 
remove_service_conf(){
    rm -rf "${SYSTEM_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}"
    rm -rf "${SYSTEMD_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}"
}

#
# Usage:
#
# Remove os configuration files (cron.d/logrotate.d)
#
remove_os_config() {
    rm -rf "${CROND_CONF}/${C_COMPONENT}-${C_PROFILE_NAME}"
}

#
# Usage:
#
# Create httpd configuration files
# 
config_httpd_profile(){
    $("${SCRIPT_PATH}"/setup-httpd.sh --action=create --profile=${C_PROFILE_NAME} --ip=${SERVER_PROFILE_IP})
	
	$("${SCRIPT_PATH}"/setup-js.sh --action=create --profile=${C_PROFILE_NAME} --type=${C_PROFILE_TYPE})
	
	config_http_defaults
}

#
# Add Proxy & SSL configuration to httpd.
#
config_http_defaults(){
    print_debug "Copy HTTP proxy config ..."

    [ -f "${HTTPD_SERVER_CONF}/httpd-proxy.conf" ] || cp "${C_SERVER_SHARE}/etc/httpd-proxy.conf" "${HTTPD_SERVER_CONF}/httpd-proxy.conf" || die "Unable to copy httpd-proxy.conf"
    
    # Setup SSL config if ssl.conf file exists
    if [ -f "${SSL_SERVER_CONF}" ]; then
        grep -q 'Include conf.d/c-' "${SSL_SERVER_CONF}" || sed -i "s/<\/VirtualHost>/Include conf.d\/${C_COMPONENT}-*\n<\/VirtualHost>/g" ${SSL_SERVER_CONF}
    fi
}

#
# Usage:
#
# Remove profile entry from ${C_COMPONENT}.conf
# 
remove_httpd_profile(){
    $("${SCRIPT_PATH}"/setup-httpd.sh --action=remove --profile=${C_PROFILE_NAME})
    
    # Remove JS static content
    $("${SCRIPT_PATH}"/setup-js.sh --action=remove --profile=${C_PROFILE_NAME})
}



# Defaults
DO_DESTRUCTIVE=0
DB_TYPE=pgsql
SERVER_ENCODING="en_US"

while [ -n "$1" ]; do
    v="${1#*=}"
    case "$1" in
		--action=*)
			ACTION="${v}"
			case "${ACTION}" in
				create|update|remove) ;;
				*) die "Illegal action parameter!"
			esac
			;;
		--profile=*)
            export C_PROFILE_NAME="${v}"
			;;
		--type=*)
			C_PROFILE_TYPE="${v}"
			case "${C_PROFILE_TYPE}" in
				c-patterns|c-retail) ;;
				*) die "Illegal type parameter!"
			esac
			;;
		--destructive)
        	DO_DESTRUCTIVE=1
        	;;
        --skip-db)
            SKIP_DB=1
            ;;
        --schema-only)
            SCHEMA_ONLY=1
            ;;
        --no-init-data)
            NO_INIT_DATA=1
            ;;
        --init-data-locale=*)
            SERVER_ENCODING="${v}"
            ;;
        --db-type=*)
            export DB_TYPE="${v}"
            case "${DB_TYPE}" in
                pgsql) ;;
                *) die "Unsupported db type $DB_TYPE"
            esac
            ;;
        --db-host=*)
            export DB_HOST="${v}"
            ;;
        --db-port=*)
            export DB_PORT="${v}"
            ;;
        --dba-pass=*)
            export DBA_PASSWORD="${v}"
            ;;
        --dba-user=*)
            export DBA_USER="${v}"
            ;;
        --debug)
            DEBUG=1
            print_debug "Debug is ON..."
            ;;  
        --help|*)
                cat <<__EOF__
Usage: $0
        --profile=profile - New server profile name
        --type=type       - New server profile type (c-patterns/c-retail)
        --action=         - action to perform
             create       - create server profile 
             remove       - remove server profile
        --destructive       - Remove profile completely
        --skip-db           - ignore any database operation 
                              (if specified, --schema-only, --no-init-data and --init-data-locale are ignored)  
        --schema-only       - Create the object definitions (schema), not tables.
        --no-init-data      - Create the schema & tables definitions, not init data.
        --init-data-locale  - Init data language locale (Default en_US)  
        --db-type=          - Database
                pgsql          - Postgres SQL (Default)
        --db-host=host      - DB host (Default localhsot)
        --db-port=port      - DB port
        --dba-user=password - DB Administrator User
        --dba-pass=password - DB Administrator Password
        --debug             - prints debug statment when such are avilable in the script
__EOF__
	exit 1
    esac
    shift
done

[ -n "${ACTION}" ] || die "Please specify action"
[ -n "${C_PROFILE_NAME}" ] || die "Please specify server profile"
[ -z "${C_PROFILE_TYPE}" ] && export C_PROFILE_TYPE="c-patterns"
[ "${ACTION}" = "create" -a -n "${C_PROFILE_NAME:20}" ] && die "Profile max length is 20 characters"
[ -z "$(echo "${C_PROFILE_NAME}" | sed 's/[a-zA-Z0-9]//g')" ] || die "Profile contains illegal character(s)"
[ "${ACTION}" = "create" -a -z "${DB_TYPE}" ] && die "Please specify db type"
[ -z "${LOOPBACK_PREFIX}" ] && LOOPBACK_PREFIX="127.0.0"
[ -z "${SERVER_ENCODING}" ] && SERVER_ENCODING="en_US"
[ -d "${SERVER_OPT}/${C_PROFILE_NAME}" ] && PROFILE_EXISTS=1
[ -z "${DB_NAME}" ] && export DB_NAME="c_application_${C_PROFILE_NAME}"

# Set defaults
export C_COMPONENT="c-application"  
export PROFILE_CONF="${SERVER_CONF}/${C_PROFILE_NAME}"
export PROFILE_RUN="${SERVER_RUN}/${C_PROFILE_NAME}"
export PROFILE_LOGS="${SERVER_LOGS}/${C_PROFILE_NAME}"
export PROFILE_STATE="${SERVER_STATE}/${C_PROFILE_NAME}"
export PROFILE_OPT="${SERVER_OPT}/${C_PROFILE_NAME}"

trap revert 1
umask 0022
main
exit 0