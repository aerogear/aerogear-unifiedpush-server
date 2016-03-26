#!/usr/bin/env bash


password="root"
mkdir -p my2mo/csvdata
cd my2mo
echo "Enter root password for MySQL:"

stty -echo 
read -p "Password: " password; echo
stty echo

mysqldump --no-data unifiedpush -u root  --password="$password" > schema.sql

../my2mo-fields.sh schema.sql

../my2mo-export.sh csvdata unifiedpush

echo "Giving MySQL Server permissions to wrtie to csvdata folder:"
sudo chown mysql:mysql csvdata/

echo "Enter root password for MySQL:"
mysql -p -u root --password="$password" < export.sql

../my2mo-import.sh csvdata unifiedpush
cd ..
echo "Removing my2mo folder"
sudo rm -rf my2mo

