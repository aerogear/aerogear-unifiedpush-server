describe('buttons', function () {

  var $scope, $compile;

  beforeEach(module('patternfly.buttons'));
  beforeEach(inject(function (_$rootScope_, _$compile_) {
    $scope = _$rootScope_;
    $compile = _$compile_;
  }));

  describe('Clear button page', function () {

    var compileButton = function (markup, scope) {
      var el = $compile(markup)(scope);
      scope.$digest();
      return el;
    };

    it('should work correctly with default model values', function () {
      $scope.model = false;
      var btn = compileButton('<button pf-btn-clear>click</button>', $scope);
      expect(btn).toHaveClass('btn');
      expect(btn).toHaveClass('btn-default');
      expect(btn).toHaveClass('btn-lg');

      $scope.model = true;
      $scope.$digest();
      expect(btn).toHaveClass('btn');
      expect(btn).toHaveClass('btn-default');
      expect(btn).toHaveClass('btn-lg');
    });

  });
});