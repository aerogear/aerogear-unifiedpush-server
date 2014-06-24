'use strict';

(function($) {

  // close dropdown immediately the menuitem is clicked, otherwise it will collide with popups/modals
  $(document).on('click', '.dropdown.open a[role="menuitem"]', function() {
    $('.dropdown.open').removeClass('open');
  });

  // focus first input in a modal once modal is shown
  angular.module('upsConsole')
    .config(function($provide) {
      $provide.decorator('$modal', function($delegate) {
        return {
          open: function() {
            var modal = $delegate.open.apply(this, arguments);
            modal.opened.then(function() {
              (function delayFocus() {
                var el = $('.modal-dialog input:first:visible');
                if (el.size() > 0) {
                  window.requestAnimationFrame(function() {
                    el.focus();
                  });
                } else {
                  window.setTimeout(delayFocus, 50);
                }
              })();
            });
            return modal;
          }
        };
      });
    });

})($);