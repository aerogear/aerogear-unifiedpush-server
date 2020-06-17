/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

class Footer extends React.Component {
  docUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    const docsUrl = this.props.config.docsUrl;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    return `${baseUrl}${docsPart}${langPart}${doc}`;
  }

  pageUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    return baseUrl + (language ? `${language}/` : '') + doc;
  }

  render() {
    const repoUrl = `https://github.com/${this.props.config.organizationName}/${this.props.config.projectName}`;

    return (
      <footer className="nav-footer" id="footer">
        <section className="sitemap">
          <a href={this.props.config.baseUrl} className="nav-home">
            {this.props.config.footerIcon && (
              <img
                src={this.props.config.baseUrl + this.props.config.footerIcon}
                alt={this.props.config.title}
                width="66"
                height="58"
              />
            )}
          </a>
          <div>
            <h5>Docs</h5>
            <a href={this.docUrl('introduction/ups-overview.html')}>
              Getting Started
            </a>
          </div>
          <div>
            <h5>Community</h5>
            <a href={`${this.props.config.baseUrl}/help`}>
              Help
            </a>
            <a href={this.props.config.mailingList}>
              AeroGear mailing list
            </a>
            <a
              href="https://stackoverflow.com/questions/tagged/aerogear"
              target="_blank"
              rel="noreferrer noopener">
              Stack Overflow
            </a>
            {/*<a href="https://discordapp.com/">Project Chat</a>*/}
          </div>
          <div>
            <h5>Social</h5>
            {/*<a href={`${this.props.config.baseUrl}blog`}>Blog</a>*/}
            <a
              className="github-button" // part of the https://buttons.github.io/buttons.js script in siteConfig.js
              href={repoUrl}
              data-count-href={`${repoUrl}/stargazers`}
              data-show-count="true"
              data-count-aria-label="# stargazers on GitHub"
              aria-label="Star this project on GitHub">
              UnifiedPush Server
            </a>
            {this.props.config.twitterUsername && (
              <div className="social">
                <a
                  href={`https://twitter.com/${this.props.config.twitterUsername}`}
                  className="twitter-follow-button">
                  Follow @{this.props.config.twitterUsername}
                </a>
              </div>
            )}
          </div>
        </section>

        <section className="copyright">{this.props.config.copyright}</section>
      </footer>
    );
  }
}

module.exports = Footer;
