package org.jboss.aerogear.unifiedpush.service.wrap;

import java.util.List;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.jboss.aerogear.unifiedpush.service.VerificationGatewayService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IVerificationGatewayService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.VerificationGatewayServiceImpl.VerificationPart;
import org.jboss.aerogear.unifiedpush.spring.SpringContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

@Stateless
@Wrapper
@Interceptors(SpringContextInterceptor.class)
public class VerificationGatewayServiceWrapper implements VerificationGatewayService {

	@Autowired
	private IVerificationGatewayService verificationGatewayService;

	@Override
	public void sendVerificationMessage(String pushApplicationId, String alias, String code) {
		verificationGatewayService.sendVerificationMessage(pushApplicationId, alias, code);
	}

	@Override
	public List<VerificationPart> getChain() {
		throw new UnsupportedOperationException("This method can be accessed from spring scope only");
	}

	@Override
	public void initializeSender() {
		throw new UnsupportedOperationException("This method can be accessed from spring scope only");
	}

}
