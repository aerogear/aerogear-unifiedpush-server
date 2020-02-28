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

function Help(props) {
  const {config: siteConfig, language = ''} = props;
  const {baseUrl, docsUrl} = siteConfig;
  const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
  const langPart = `${language ? `${language}/` : ''}`;
  const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;

  const supportLinks = [
    {
      content: `Learn more about UnifiedPush Server using the [official documentation](${docUrl(
        'introduction/ups-overview',
      )}).`,
      title: 'Browse the docs',
    },
    {
      title: 'Discord',
      content: 'You can join the conversation on [Discord](https://discord.gg/mJ7j84m) in our channel: #unified-push'
    },
    {
      content: 'You can follow and contact us on [Twitter](https://twitter.com/aerogears).',
      title: 'Twitter',
    },
    {
      content: 'At our [GitHub repo](https://github.com/aerogear/aerogear-unifiedpush-server) Browse and submit ' +
          '[issues](https://github.com/aerogear/aerogear-unifiedpush-server/issues) ' +
          'or [pull requests](https://github.com/aerogear/aerogear-unifiedpush-server/pulls) for bugs you find or ' +
          'any new features you may want implemented. Be sure to also check out our ' +
          '[contributing information](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/.github/CONTRIBUTING.md)' +
          'and out [code of conduct](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/.github/CODE_OF_CONDUCT.md).',
      title: 'GitHub'
    },
    {
      content: 'If you’ve got ideas for the AeroGear project or want to share what you\'re working on or struggling with, ' +
          'the <a href="https://groups.google.com/forum/#!forum/aerogear">AeroGear Google Group</a> is a good place to ' +
          'start. The Mailing List is used for both team-wide and community communication.',
      title: 'Join the community',
    },
  ];
    

  return (
    <div className="docMainWrapper wrapper">
      <Container className="mainContainer documentContainer postContainer">
        <div className="post">
          <header className="postHeader">
            <h1>Need help?</h1>
          </header>
          <p>This project is maintained by a dedicated group of people.</p>
          <GridBlock contents={supportLinks} layout="fourColumn" />
        </div>
      </Container>
    </div>
  );
}

module.exports = Help;
