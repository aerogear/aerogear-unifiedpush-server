angular.module('upsConsole')
  .controller('AnalyticsController', function ( $scope, $q, metricsEndpoint, c3Factory ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;

    this.metrics = [];
    this.totalCount = 0;
    this.receivers = 0;
    this.appOpenedCounter = 0;
    this.appOpenedRate = 0;

    this.platforms = {};
    this.platformArray = [];

    this.performance = [
      ['Targeted Devices'],
      ['Push Open']
    ];

    function updateAnalytics() {
      metricsEndpoint.fetchApplicationMetrics(self.app.pushApplicationID, null, 1, 6)
        .then(function (data) {
          self.metrics = data.pushMetrics.reverse();
          self.totalCount = data.totalItems;
          self.receivers = data.receivers;
          self.appOpenedCounter = data.appOpenedCounter;
          self.appOpenedRate = ((data.appOpenedCounter * 100) / (data.receivers)) || 0;

          self.performance = [
            ['Targeted Devices'],
            ['Push Open']
          ];
          angular.forEach(self.metrics, function (pushMessage) {
            pushMessage.message = JSON.parse(pushMessage.rawJsonMessage);
            self.performance[0].push(pushMessage.totalReceivers);
            self.performance[1].push(pushMessage.appOpenCounter);
          });

          c3Factory.get('performance').then(function (chart) {
            chart.load({
              columns: self.performance
            })
          });
        });
    }

    updateAnalytics();

    $scope.$on('upsNotificationSent', function( pushData, app ) {
      updateAnalytics();
    });

    angular.forEach(this.app.variants, function(variant) {
      var platform = getPlatform(variant);
      self.platforms[platform] = (self.platforms[platform] || 0) + parseInt(variant.$deviceCount);
    });
    angular.forEach(self.platforms, function(value, key) {
      self.platformArray.push([key, value]);
    });

    function getPlatform(variant) {
      switch(variant.type) {
        case 'adm': return 'Amazon';
        case 'ios': return 'iOS';
        case 'simplePush': return 'SimplePush';
        case 'windows_mpns': return 'Windows';
        case 'windows_wns': return 'Windows';
        case 'android': return 'Android';
        default: return variant.type;
      }
    }

    this.performanceChartConfig = {
      axis: {
        rotated: false,
        x: {
          type: 'category',
          categories: ['Message 01', 'Message 02', 'Message 03', 'Message 04', 'Message 05', 'Message 06'],
          tick: {
            outer: false,
            format: function (d) {
              var pushMessage = self.metrics[d];
              if (pushMessage) {
                var alert = pushMessage.message.alert;
                if (alert.length > 15) {
                  alert = alert.substring(0, 15) + '...';
                }
                return alert;
              } else {
                return 'Message #' + d;
              }
            }
          }
        },
        y: {
          tick: {
            outer: false,
            format: function (x) {
              if (x != Math.floor(x)) {
                var tick = d3.selectAll('.c3-axis-y g.tick').filter(function () {
                  var text = d3.select(this).select('text').text();
                  return +text === x;
                }).style('opacity', 0);
                return '';
              }
              return x;
            }
          }
        }
      },
      color: {
        pattern: ['#006e9c','#00a8e1', '#3f9c35', '#ec7a08', '#cc0000']
      },
      data: {
        columns: this.performance,
        type: 'bar'
      },
      grid: {
        y: {
          show: true
        }
      }
    };

    this.pushNetworksChartConfig = {
      data: {
        colors: {
          iOS: '#006e9c',
          Android: '#00a8e1',
          Windows: '#969696'
        },
        columns: this.platformArray,
        type : 'donut'
      },
      donut: {
        title: function() {
          if (self.app.$deviceCount == 1) {
            return '1 Device';
          } else {
            return self.app.$deviceCount + ' Devices';
          }
        }
      }
    };

  });
