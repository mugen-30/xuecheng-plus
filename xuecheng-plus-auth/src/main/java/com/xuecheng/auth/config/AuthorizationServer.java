package com.xuecheng.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import javax.annotation.Resource;

/**
 * @author mugen
 * @description 授权服务器配置
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Resource(name = "authorizationServerTokenServicesCustom")
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Resource
    private AuthenticationManager authenticationManager;

    //客户端详情服务
    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception {
        clients.inMemory() // 使用in-memory存储
                .withClient("XcWebApp") // client_id
                // .secret("XcWebApp") //客户端密钥（未使用明文密码）
                .secret(new BCryptPasswordEncoder().encode("XcWebApp")) //客户端密钥（使用BCrypt加密）
                .resourceIds("xuecheng-plus") //资源列表
                .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token") // 该client允许的授权类型：authorization_code, password, refresh_token, implicit, client_credentials
                .scopes("all") // 允许的授权范围
                .autoApprove(false) // false表示跳转到授权页面进行手动授权
                //客户端接收授权码的重定向地址
                .redirectUris("http://www.51xuecheng.cn")
        ;
    }



    // 令牌端点的访问配置
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                // 设置认证管理器
                .authenticationManager(authenticationManager)// 认证管理器
                // 设置令牌管理服务
                .tokenServices(authorizationServerTokenServices)// 令牌管理服务
                // 允许在令牌端点使用POST方法
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }


    // 令牌端点的安全配置
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                // 允许所有用户访问oauth/token_key端点
                .tokenKeyAccess("permitAll()")                    // oauth/token_key是公开的
                // 允许所有用户访问oauth/check_token端点
                .checkTokenAccess("permitAll()")                  // oauth/check_token是公开的
                // 允许客户端通过表单认证方式申请令牌
                .allowFormAuthenticationForClients()                // 表单认证（申请令牌）
        ;
    }



}
