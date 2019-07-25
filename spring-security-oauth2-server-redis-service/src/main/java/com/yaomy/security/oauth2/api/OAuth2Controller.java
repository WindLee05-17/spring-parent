package com.yaomy.security.oauth2.api;

import com.google.common.collect.Maps;
import com.yaomy.common.enums.HttpStatusMsg;
import com.yaomy.common.po.BaseResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.OAuth2AccessTokenSupport;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description: 端点访问控制包装类 示例：https://www.programcreek.com/java-api-examples/?code=h819/spring-boot/spring-boot-master/spring-security-oauth/spring-security-oauth2-client/src/main/java/com/base/oauth2/client/controller/SpringOauth2ClientController.java#
 * @ProjectName: spring-parent
 * @Package: com.yaomy.security.oauth2.api.OAuth2Controller
 * @Date: 2019/7/22 15:57
 * @Version: 1.0
 */
@RestController
@RequestMapping(value = "oauth2")
public class OAuth2Controller implements InitializingBean {

    @Value("${oauth.token.uri}")
    private String tokenUri;

    @Value("${oauth.resource.id}")
    private String resourceId;

    @Value("${oauth.resource.client.id}")
    private String resourceClientId;

    @Value("${oauth.resource.client.secret}")
    private String resourceClientSecret;
    @Autowired
    @Lazy
    private TokenStore tokenStore;

    /**
     * @Description 获取token信息
     * @Date 2019/7/22 15:59
     * @Version  1.0
     */
    @RequestMapping(value = "token", method = RequestMethod.POST)
    public ResponseEntity<BaseResponse> getToken(@RequestParam String username, @RequestParam String password){
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        resource.setId(resourceId);
        resource.setClientId(resourceClientId);
        resource.setClientSecret(resourceClientSecret);
        resource.setGrantType("password");
        resource.setAccessTokenUri(tokenUri);
        resource.setUsername(username);
        resource.setPassword(password);
        resource.setScope(Arrays.asList("test"));

        OAuth2RestTemplate template = new OAuth2RestTemplate(resource);
        ResourceOwnerPasswordAccessTokenProvider provider = new ResourceOwnerPasswordAccessTokenProvider();
        template.setAccessTokenProvider(provider);
        System.out.println("过期时间是："+template.getAccessToken().getExpiration());
        BaseResponse response = null;
        try {
            response = BaseResponse.createResponse(HttpStatusMsg.OK, template.getAccessToken());
        } catch (Exception e){
            response = BaseResponse.createResponse(HttpStatusMsg.AUTHENTICATION_EXCEPTION, e.toString());
        }
        return ResponseEntity.ok(response);
    }
    @RequestMapping(value = "refresh_token", method = RequestMethod.POST)
    public ResponseEntity<Map> refreshToken(String refresh_token){

        Map<String, Object> param = Maps.newHashMap();
        param.put("client_id", resourceClientId);
        param.put("client_secret", resourceClientSecret);
        param.put("grant_type", "refresh_token");
        param.put("refresh_token", refresh_token);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> result = restTemplate.postForEntity(tokenUri, param, Map.class);

        return result;
    }
    @RequestMapping(value = "check_token", method = RequestMethod.POST)
    public ResponseEntity<Map> checkToken(String check_token){
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        OAuth2RestTemplate template = new OAuth2RestTemplate(resource);
        template.setAccessTokenProvider(new ResourceOwnerPasswordAccessTokenProvider());
        Map<String, Object> param = Maps.newHashMap();
        param.put("token", check_token);

        ResponseEntity<Map> result = template.postForEntity(tokenUri, param, Map.class);

        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("init OAuth2Controller-----------");
    }
}
