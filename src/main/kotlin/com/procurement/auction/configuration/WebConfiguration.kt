package com.procurement.auction.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = ["com.procurement.auction.infrastructure.web.dispatcher"])
class WebConfiguration : WebMvcConfigurer