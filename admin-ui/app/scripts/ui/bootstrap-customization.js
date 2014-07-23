'use strict';

(function($) {

  // close dropdown immediately the menuitem is clicked, otherwise it will collide with popups/modals
  $(document).on('click', '.dropdown.open a[role="menuitem"]', function() {
    $('.dropdown.open').removeClass('open');
  });

})($);