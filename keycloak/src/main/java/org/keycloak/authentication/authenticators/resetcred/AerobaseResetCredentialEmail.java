package org.keycloak.authentication.authenticators.resetcred;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.actiontoken.DefaultActionTokenKey;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

public class AerobaseResetCredentialEmail extends ResetCredentialEmail {
	private static final Logger logger = Logger.getLogger(AerobaseResetCredentialEmail.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        String username = authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        // we don't want people guessing usernames, so if there was a problem obtaining the user, the user will be null.
        // just reset login for with a success message
        if (user == null) {
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
            return;
        }

        String actionTokenUserId = authenticationSession.getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);
        if (actionTokenUserId != null && Objects.equals(user.getId(), actionTokenUserId)) {
            logger.debugf("Forget-password triggered when reauthenticating user after authentication via action token. Skipping " + PROVIDER_ID + " screen and using user '%s' ", user.getUsername());
            context.success();
            return;
        }


        EventBuilder event = context.getEvent();
        // we don't want people guessing usernames, so if there is a problem, just continuously challenge
        if (user.getEmail() == null || user.getEmail().trim().length() == 0) {
            event.user(user)
                    .detail(Details.USERNAME, username)
                    .error(Errors.INVALID_EMAIL);

            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
            return;
        }

        int validityInSecs = context.getRealm().getActionTokenGeneratedByUserLifespan();
        int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;

        // We send the secret in the email in a link as a query param.
        ResetCredentialsActionToken token = new ResetCredentialsActionToken(user.getId(), absoluteExpirationInSecs, authenticationSession.getId());
        String link = UriBuilder
          .fromUri(context.getActionTokenUrl(token.serialize(context.getSession(), context.getRealm(), context.getUriInfo())))
          .build()
          .toString();

        link = linkByOrigin(link, context);
        long expirationInMinutes = TimeUnit.SECONDS.toMinutes(validityInSecs);
        try {
            context.getSession().getProvider(EmailTemplateProvider.class).setRealm(context.getRealm()).setUser(user).sendPasswordReset(link, expirationInMinutes);

            event.clone().event(EventType.SEND_RESET_PASSWORD)
                         .user(user)
                         .detail(Details.USERNAME, username)
                         .detail(Details.EMAIL, user.getEmail()).detail(Details.CODE_ID, authenticationSession.getId()).success();
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
        } catch (EmailException e) {
            event.clone().event(EventType.SEND_RESET_PASSWORD)
                    .detail(Details.USERNAME, username)
                    .user(user)
                    .error(Errors.EMAIL_SEND_FAILED);
            ServicesLogger.LOGGER.failedToSendPwdResetEmail(e);
            Response challenge = context.form()
                    .setError(Messages.EMAIL_SENT_ERROR)
                    .createErrorPage();
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
        }
    }

    private String linkByOrigin(String link, AuthenticationFlowContext context){
    	String origin = context.getHttpRequest().getHttpHeaders().getHeaderString("origin");
    	if (origin == null || origin.length() == 0) {
    		return link;
    	}

    	if (!origin.endsWith("/")){
    		origin += "/";
    	}

    	// Modify email link, use referer header
    	return link.replaceFirst(context.getUriInfo().resolve(URI.create("/")).toString(), origin);
    }
}
