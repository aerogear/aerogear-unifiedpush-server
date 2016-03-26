#!/usr/bin/make -f

LBINDIR = usr/local/bin
MANDIR = usr/share/man/man1

all:

install:
	# Create directories
	install -d $(DESTDIR)/$(LBINDIR)
	install -d $(DESTDIR)/$(MANDIR)

	# Install user scripts
	install -m 755 my2mo-fields.sh $(DESTDIR)/$(LBINDIR)/my2mo-fields
	install -m 755 my2mo-export.sh $(DESTDIR)/$(LBINDIR)/my2mo-export
	install -m 755 my2mo-import.sh $(DESTDIR)/$(LBINDIR)/my2mo-import

	# Install man page
	gzip -c man/my2mo-fields.1 > $(DESTDIR)/$(MANDIR)/my2mo-fields.1.gz
	gzip -c man/my2mo-export.1 > $(DESTDIR)/$(MANDIR)/my2mo-export.1.gz
	gzip -c man/my2mo-import.1 > $(DESTDIR)/$(MANDIR)/my2mo-import.1.gz

uninstall:
	# Remove user scripts
	-rm -f  $(DESTDIR)/$(LBINDIR)/my2mo-fields
	-rm -f  $(DESTDIR)/$(LBINDIR)/my2mo-export
	-rm -f  $(DESTDIR)/$(LBINDIR)/my2mo-import

	# Remove man page
	-rm -f $(DESTDIR)/$(MANDIR)/my2mo-fields.1.gz
	-rm -f $(DESTDIR)/$(MANDIR)/my2mo-export.1.gz
	-rm -f $(DESTDIR)/$(MANDIR)/my2mo-import.1.gz

help2man:
	help2man -n "mysql-to-mongo script to create table and fields files from database schema" -s 1 -N -o man/my2mo-fields.1 "bash my2mo-fields.sh"
	help2man -n "mysql-to-mongo script to generate SQL to export tables from MySQL" -s 1 -N -o man/my2mo-export.1 "bash my2mo-export.sh"
	help2man -n "mysql-to-mongo script to import data files with mongoimport" -s 1 -N -o man/my2mo-import.1 "bash my2mo-import.sh"
