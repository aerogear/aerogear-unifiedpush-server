#!/bin/bash
#
# Simplify importing data files with mongoimport.
#
# Copyright (c) 2011 Lance Lovette. All rights reserved.
# Licensed under the BSD License.
# See the file LICENSE.txt for the full license text.
#
# Available from https://github.com/lovette/mysql-to-mongo

CMDPATH=$(readlink -f "$0")
CMDNAME=$(basename "$CMDPATH")
CMDDIR=$(dirname "$CMDPATH")
CMDARGS=$@

MY2MO_EXPORT_VER="1.0.2"

OUTPUTDIR="."
GETOPT_TABDELIMITED=0

##########################################################################
# Functions

# echo_stderr(string)
# Outputs message to stderr
function echo_stderr()
{   
	echo $* 1>&2
}
    
# exit_arg_error(string)
# Outputs message to stderr and exits
function exit_arg_error()
{
	local message="$1"

	[ -n "$message" ] && echo_stderr "$CMDNAME: $message"
	echo_stderr "Try '$CMDNAME --help' for more information."
	exit 1
}   
    
# exit_error(string)
# Outputs message to stderr and exits
function exit_error()
{ 
	local message="$1" 

	[ -n "$message" ] && echo_stderr "$CMDNAME: $message"
	exit 1
} 

# array_join(glue, array)
# Outputs array elements joined by glue
function array_join()
{
	local arr=( )
	local glue=","
	local str=""
	local i

	glue="$1"
	shift
	arr=( "$@" )

	for i in "${arr[@]}"
	do
		str="${str}${glue}$i"
	done

	echo ${str/$glue/}
}

# gettablenames(path)
# Outputs table names (first column) from 'path'
function gettablenames()
{
	grep -v "^#" "$1" | cut -d" " -f1
}

# getselectexpr(path)
# Outputs field name or expression from 'path'
function getselectexpr()
{
	grep -v "^#" "$1" | cut -d" " -f2-
}

# Print version and exit
function version()
{
	echo "my2mo-export $MY2MO_EXPORT_VER"
	echo
	echo "Copyright (C) 2011 Lance Lovette"
	echo "Licensed under the BSD License."
	echo "See the distribution file LICENSE.txt for the full license text."
	echo
	echo "Written by Lance Lovette <https://github.com/lovette>"

	exit 0
}   

# Print usage and exit
function usage()
{
	echo "Reads an 'import.tables' file and set of fields files and creates"
	echo "an 'export.sql' file containing 'SELECT INTO OUTFILE' statements"
	echo "for use by MySQL to create comma or tab-delimited data files for each table."
	echo "The 'import.tables' and fields files can be created from scratch or"
	echo "from an SQL database schema by my2mo-fields. The data files generated"
	echo "can be imported into a MongoDB database by my2mo-import."
	echo
	echo "Usage: my2mo-export [OPTION]... CSVDIR EXPORTDB"
	echo
	echo "Options:"
	echo "  CSVDIR         Directory where exported data files will be written"
	echo "  EXPORTDB       MySQL database to export"
	echo "  -d DIRECTORY   Directory with import.tables and fields directory"
	echo "  -h, --help     Show this help and exit"
	echo "  -t             Create tab-delimited data files"
	echo "  -V, --version  Print version and exit"
	echo
	echo "Report bugs to <https://github.com/lovette/mysql-to-mongo/issues>"

	exit 0
}

##########################################################################
# Main


# Check for usage longopts
case "$1" in
	"--help"    ) usage;;
	"--version" ) version;;
esac

# Parse command line options
while getopts "d:htV" opt
do
	case $opt in
	d  ) OUTPUTDIR="$OPTARG";;
	h  ) usage;;
	t  ) GETOPT_TABDELIMITED=1;;
	V  ) version;;
	\? ) exit_arg_error;;
	esac
done

shift $(($OPTIND - 1))

CSVDIR="$1"
EXPORTDB="$2"

[ -n "$CSVDIR" ] || exit_arg_error "missing data directory"
[ -n "$EXPORTDB" ] || exit_arg_error "missing export database name"

# Convert to real path
[ -d "$OUTPUTDIR" ] && OUTPUTDIR=$(readlink -f "$OUTPUTDIR")
[ -d "$CSVDIR" ] && CSVDIR=$(readlink -f "$CSVDIR")

TABLESPATH="$OUTPUTDIR/import.tables"
FIELDSDIR="$OUTPUTDIR/fields"
SQLPATH="$OUTPUTDIR/export.sql"

[ -d "$OUTPUTDIR" ] || exit_error "$OUTPUTDIR: No such directory"
[ -w "$OUTPUTDIR" ] || exit_error "$OUTPUTDIR: Write permission denied"
[ -d "$FIELDSDIR" ] || exit_error "$FIELDSDIR: No such directory"
[ -r "$FIELDSDIR" ] || exit_error "$FIELDSDIR: Read permission denied"
[ -f "$TABLESPATH" ] || exit_error "$TABLESPATH: No such file"
[ -r "$TABLESPATH" ] || exit_error "$TABLESPATH: Read permission denied"

# Export directory does not have to exist now, but it's best practice 
# to use full path since it's run on the MySQL server
[[ "$CSVDIR" = /* ]] || exit_error "$CSVDIR: Export directory cannot be a relative path"

TABLES=( $(gettablenames "$TABLESPATH") )
[ $? -eq 0 ] || exit_error
[ ${#TABLES[@]} -gt 0 ] || exit_error "No tables found"

echo "Generating SQL to export ${#TABLES[@]} tables..."

echo "# Execute this script with 'mysql' to export table data to '$CSVDIR'" > "$SQLPATH"
echo "USE $EXPORTDB;" >> "$SQLPATH"

for table in "${TABLES[@]}"
do
	csvpath="$CSVDIR/$table.csv"
	fieldpath="$FIELDSDIR/$table.fields"

	# Convert fields/expressions into a comma-delimited list
	OLD_IFS="$IFS"
	IFS=$'\n'
	fields=( $(getselectexpr "$fieldpath") )
	IFS="$OLD_IFS"
	fields=$(array_join ", " "${fields[@]}")

	[ -n "$fields" ] || exit_error "...$table, no export fields defined!"

	# Each table can have a SQL clause
	tablesql=$(awk "{ if (\$1 == \"$table\") print }" "$TABLESPATH" | cut -d" " -f2- -s)
	[ -z "$tablesql" ] || tablesql=" ${tablesql}"

	if [ $GETOPT_TABDELIMITED -eq 0 ]; then
		echo "SELECT $fields FROM ${table}${tablesql} INTO OUTFILE \"$csvpath\" FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n';" >> "$SQLPATH"
	else
		echo "SELECT $fields FROM ${table}${tablesql} INTO OUTFILE \"$csvpath\" FIELDS TERMINATED BY '\\t' ENCLOSED BY '' ESCAPED BY '\\\\' LINES TERMINATED BY '\\n';" >> "$SQLPATH"
	fi
done

echo "export.sql saved to $OUTPUTDIR"
echo "Data files will be saved to $CSVDIR on the"
echo "MySQL server, make sure this directory exists, and is empty"

exit 0
