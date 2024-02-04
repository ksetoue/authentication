package io.ksetoue.login.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.util.stream.Collectors

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(private val keycloakLogoutHandler: KeycloakLogoutHandler) {
    @Bean
    protected fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return RegisterSessionAuthenticationStrategy(SessionRegistryImpl())
    }

    @Bean
    @Throws(Exception::class)
    fun resourceServerFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests(Customizer { auth ->
            auth
                .requestMatchers(AntPathRequestMatcher("/customers*"))
                .hasRole("user")
                .requestMatchers(AntPathRequestMatcher("/"))
                .permitAll()
                .anyRequest()
                .authenticated() as AuthorizationManagerRequestMatcherRegistry
        })
        http.oauth2ResourceServer { oauth2: OAuth2ResourceServerConfigurer<HttpSecurity?> ->
            oauth2.jwt(Customizer.withDefaults())
        }

        http.oauth2Login(Customizer.withDefaults())
            .logout { logout: LogoutConfigurer<HttpSecurity?> ->
                logout.addLogoutHandler(
                    keycloakLogoutHandler
                ).logoutSuccessUrl("/")
            }
        return http.build()
    }

    @Bean
    fun userAuthoritiesMapperForKeycloak(): GrantedAuthoritiesMapper {
        return GrantedAuthoritiesMapper { authorities: Collection<GrantedAuthority> ->
            val mappedAuthorities = mutableSetOf<GrantedAuthority>()
            val authority = authorities.iterator().next()
            val isOidc = authority is OidcUserAuthority

            val oauth2UserAuthority = authority as OAuth2UserAuthority
            val userAttributes = oauth2UserAuthority.attributes
            if (userAttributes.containsKey(REALM_ACCESS_CLAIM)) {
                val realmAccess = userAttributes[REALM_ACCESS_CLAIM] as Map<*, *>?
                val roles =
                    realmAccess!![ROLES_CLAIM] as Collection<*>?
                mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles))
            }

            if (isOidc) {
                val oidcUserAuthority = authority as OidcUserAuthority
                val userInfo = oidcUserAuthority.userInfo

                // Tokens can be configured to return roles under
                // Groups or REALM ACCESS hence have to check both
                if (userInfo.hasClaim(REALM_ACCESS_CLAIM)) {
                    val realmAccess = userInfo.getClaimAsMap(REALM_ACCESS_CLAIM)
                    val roles = realmAccess[ROLES_CLAIM] as Collection<*>?
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles))
                }

                if (userInfo.hasClaim(GROUPS)) {
                    val roles = userInfo.getClaim<Any>(GROUPS) as Collection<*>
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles))
                }
            }

            mappedAuthorities
        }

    }

    fun generateAuthoritiesFromClaim(roles: Collection<*>?): Collection<GrantedAuthority> {
        return roles!!.stream().map { role -> SimpleGrantedAuthority("ROLE_$role") }
            .collect(Collectors.toList())
    }

    companion object {
        private const val GROUPS = "groups"
        private const val REALM_ACCESS_CLAIM = "realm_access"
        private const val ROLES_CLAIM = "roles"
    }
}