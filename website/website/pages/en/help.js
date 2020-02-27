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
      content: `If you would like to learn more about UnifiedPush Server, [documentation on this site.](${docUrl(
        'introduction/ups-overview',
      )})`,
      title: 'Browse Docs',
    },
    {
      content: 'If you’ve got ideas for the AeroGear project or want to share what you\'re working on or struggling with, ' +
          'the <a href="https://groups.google.com/forum/#!forum/aerogear">AeroGear Google Group</a> is a good place to ' +
          'start. The Mailing List is used for both team-wide and community communication.',
      title: 'Join the community',
    },
    {
      content: ' The AeroGear community is an open and inclusive environment, we expect members to be ' +
      ' acquainted with our <a href="https://github.com/Aindriunannerb/aerogear-unifiedpush-server/blob/master/.github/CODE_OF_CONDUCT.md">Code of Conduct</a>',
      title: 'Code of Conduct',
    },
    {
      content: 'The UnifiedPush Server Github Repository can be found <a href="https://github.com/aerogear/aerogear-unifiedpush-server">here.</a> ' +
      'If you would like to find more great projects within the AeroGear Organization check out our <a href="https://github.com/aerogear">GitHub.</a>' +
      ' If you want to dive right into UnifiedPush, the <a href="https://github.com/aerogear/unifiedpush-cookbook">UnifiedPush Cookbook</a> repository contains' + 
      ' simple example applications that illustrate how to integrate' +
      ' UnifiedPush Server into your own application',
      title: 'AeroGear Repositories'
    }
  ]
    

  return (
    <div className="docMainWrapper wrapper">
      <Container className="mainContainer documentContainer postContainer">
        <div className="post">
          <header className="postHeader">
            <h1>Need help?</h1>
          </header>
          <p>This project is maintained by a dedicated group of people.</p>
          <GridBlock contents={supportLinks} layout="twoColumn" />
        </div>
      </Container>
    </div>
  );
}

module.exports = Help;
