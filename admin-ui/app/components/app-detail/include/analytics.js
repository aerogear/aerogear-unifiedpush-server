angular.module('upsConsole')
  .controller('AnalyticsController', function ( $scope ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;

    this.config = {
      axis: {
        rotated: false,
        x: {
          categories: ['Message 01', 'Message 02', 'Message 03', 'Message 04', 'Message 05', 'Message 06'],
          tick: {
            outer: false
          },
          type: 'category'
        },
        y: {
          tick: {
            outer: false
          }
        }
      },
      bindto: '#chart6',
      color: {
        pattern: ['#006e9c','#00a8e1', '#3f9c35', '#ec7a08', '#cc0000']
      },
      data: {
        columns: [
          ['Push notifications', 2400, 3190, 3210, 2100, 3950, 2050],
          ['Push Open', 1250, 1820, 1900, 1240, 2115, 1020],
//              ['App Opens', 1450, 2020, 2100, 1840, 2315, 1520],
        ],
//            groups: [
//              ['Delivered notifications', 'Open notifications']
//            ],
        type: 'bar'
      },
      grid: {
        y: {
          show: true
        }
      }
    };

    this.config2 = {
      bindto: '#chart8',
      data: {
        colors: {
          iOS: '#006e9c',
          Android: '#00a8e1',
          Windows: '#969696'
        },
        columns: [
          ['iOS', 20283563],
          ['Android', 15000000],
          ['Windows', 5000000]
        ],
        type : 'donut',
        onclick: function (d, i) { console.log("onclick", d, i); },
        onmouseover: function (d, i) { console.log("onmouseover", d, i); },
        onmouseout: function (d, i) { console.log("onmouseout", d, i); }
      },
      donut: {
        title: "50,283,563 Devices"
      }
    };

  });
