VERSION=v1.0.0-rc.6

default: emberjs
	@cd $< && git pull && git checkout $(VERSION) && bundle install && rake dist
	@cp -f $</dist/ember.js .
	@cp -f $</dist/ember.min.js .
	@du -bh ember.*

emberjs:
	@git clone https://github.com/emberjs/ember.js.git $@

.PHONY: default
