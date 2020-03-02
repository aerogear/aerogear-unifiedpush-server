/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');
const CompLibrary = require('../../core/CompLibrary.js');
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class Index extends React.Component {
  render() {
    const {config: siteConfig, language = ''} = this.props;
    const {baseUrl} = siteConfig;

    const Block = props => (
      <Container
        padding={['bottom']}
        id={props.id}
        background={props.background}>
        <GridBlock
          align="center"
          contents={props.children}
          layout={props.layout}
        />
      </Container>
    );

    const Features = () => (
        <div>
      <Block layout="threeColumn">
          {[
            {
              content: 'A server written in Java which allow to send push notifications to different mobile operating systems and web application',
              image: `${baseUrl}img/notification.svg`,
              imageLink: '/docs/introduction/ups-overview',
              imageAlign: 'top',
              title: '<a href="/docs/introduction/ups-overview">UnifiedPush Server</a>',
            },
          ]}
      </Block>
      <Block layout="fourColumn">
        {[
          {
            content: 'A Java library that allows the integration of the UnifiedPush Server into your native Android application',
            image: `${baseUrl}img/java.svg`,
            imageAlign: 'top',
            imageLink: '/docs/clients/android-client',
            title: '<a href="/docs/clients/android-client">Java Client SDK</a>',
          },
          {
            content: 'A Swift library that allows the integration of the UnifiedPush Server into your native iOS application',
            image: `${baseUrl}img/swift.svg`,
            imageAlign: 'top',
            imageLink: '/docs/clients/ios-client#ios',
            title: '<a href="/docs/clients/ios-client#ios">Swift Client SDK</a>',
          },
          {
            content: 'A JavaScript library that allows the integration of the UnifiedPush Server into your JavaScript application (WebApplication, Cordova/Ionic)',
            image: `${baseUrl}img/javascript.svg`,
            imageAlign: 'top',
            imageLink: '/docs/clients/webpush-client#webpush',
            title: '<a href="/docs/clients/webpush-client#webpush">JS Client SDK</a>',
          },
          {
            content: 'A cordova plugin that plugs our JavaScript library into your multi platform Cordova application',
            image: `${baseUrl}img/cordova.svg`,
            imageAlign: 'top',
            imageLink: '/docs/clients/cordova-client#cordova',
            title: '<a href="/docs/clients/cordova-client#cordova">Cordova Client SDK</a>',
          },
          {
            content: 'A Java library that allows to easily send push notification from your own application',
            image: `${baseUrl}img/java.svg`,
            imageAlign: 'top',
            imageLink: '/docs/server_sdk/javasender#javasender',
            title: '<a href="/docs/server_sdk/javasender#javasender">Java Sender API</a>',
          },
          {
            content: 'A Node.js package that allows to easily send push notification from your own application',
            image: `${baseUrl}img/nodejs-icon.svg`,
            imageAlign: 'top',
            imageLink: '/docs/server_sdk/nodesender#node-sender',
            title: '<a href="/docs/server_sdk/nodesender#node-sender">Node.js Sender API</a>',
          },
          {
            content: 'A set or rest endpoints that allows to easily send push notification from your own application',
            image: `${baseUrl}img/rest-api.svg`,
            imageAlign: 'top',
            imageLink: '/docs/server_sdk/restfulsender#rest-sender',
            title: '<a href="/docs/server_sdk/restfulsender#node-sender">REST Sender API</a>',
          },
        ]}
      </Block>
            </div>
    );

    return (
      <div>
        <div className="mainContainer">
          <Features />
        </div>
      </div>
    );
  }
}

module.exports = Index;
