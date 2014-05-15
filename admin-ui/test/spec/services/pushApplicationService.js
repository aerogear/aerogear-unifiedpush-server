'use strict';

describe('Service: pushApplicationService', function () {

  // load the service's module
  beforeEach(module('newadminApp'));

  // instantiate service
  var pushApplicationService;
  beforeEach(inject(function (_pushApplicationService_) {
    pushApplicationService = _pushApplicationService_;
  }));

  it('should do something', function () {
    expect(!!pushApplicationService).toBe(true);
  });

});
