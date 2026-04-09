# ----------------------------------------------------------------------------------------
#
# Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
#
# This software is the property of WSO2 LLC. and its suppliers, if any.
# Dissemination of any information or reproduction of any material contained
# herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
# You may not alter or remove any copyright or other notice from copies of this content.
#
# ----------------------------------------------------------------------------------------

# Define dynamic registry and version arguments globally
ARG BASE_DOCKER_IMAGE_REGISTRY="registry.wso2.com"
ARG BASE_PRODUCT_VERSION

# ----------------------------------------------
# STAGE 1: ACCELERATOR SOURCE (obiam)
# Uses a pre-built WSO2 Open Banking image that contains the extracted
# accelerator files. This is faster and more reliable than downloading.
# ----------------------------------------------
FROM ${BASE_DOCKER_IMAGE_REGISTRY}/wso2-ob/obiam-accelerator:4.0.0.0-alpine AS obiam

# Note: The test command verifies the expected accelerator directory exists
# This assumes the pre-built image structure is standardized.
RUN test -d /home/wso2carbon/wso2-fsiam-accelerator-4.0.0

# ----------------------------------------------
# STAGE 2: FINAL WSO2 IS IMAGE (Merge Accelerator)
# Sets up the base product and integrates the accelerator artifacts.
# ----------------------------------------------
FROM ${BASE_DOCKER_IMAGE_REGISTRY}/wso2-is/is:${BASE_PRODUCT_VERSION}.0-alpine
LABEL maintainer="WSO2 Docker Maintainers <dev@wso2.org>" \
      com.wso2.docker.source="https://github.com/wso2/docker-open-banking/releases/tag/v4.0.0.3"

# --- Build Arguments (Shared across WSO2 images) ---
# build arguments for user/group configurations
ARG USER=wso2carbon
ARG USER_ID=802
ARG USER_GROUP=wso2
ARG USER_GROUP_ID=802
ARG USER_HOME=/home/${USER}
# build arguments for WSO2 product installation
ARG BASE_PRODUCT_VERSION
ARG WSO2_SERVER=wso2is-${BASE_PRODUCT_VERSION}
ARG WSO2_OB_ACCELERATOR=wso2-fsiam-accelerator-4.0.0
ARG WSO2_SERVER_HOME=${USER_HOME}/${WSO2_SERVER}
ARG WSO2_OB_ACCELERATOR_DIR=${WSO2_SERVER_HOME}/${WSO2_OB_ACCELERATOR}

ARG OB_TRUSTED_CERTS_URL
ARG WSO2_OB_KEYSTORES_URL
ARG RESOURCE_URL

# Ensure we are root to install tools and manage permissions if needed
USER root

# Install bash temporarily since the WSO2 IS Alpine base image might use ash, but the merge script expects bash syntax
RUN apk update && apk add bash

# --- Copy and Install Accelerator ---

# Copy the extracted accelerator folder from the 'obiam' stage into the WSO2 IS home directory
# Source: /home/wso2carbon/wso2-fsiam-accelerator-4.0.0 from the obiam image
# Destination: /home/wso2carbon/wso2is-7.1.0/wso2-fsiam-accelerator-4.0.0 in the final image
COPY --from=obiam ${USER_HOME}/${WSO2_OB_ACCELERATOR} ${WSO2_SERVER_HOME}/${WSO2_OB_ACCELERATOR}

# add keystores to base product
RUN if [ -n "$WSO2_OB_KEYSTORES_URL" ]; then wget -O private-keys.jks "${WSO2_OB_KEYSTORES_URL}/private-keys.jks"  \
    && yes | keytool -importkeystore -srckeystore ${USER_HOME}/private-keys.jks -destkeystore ${WSO2_SERVER_HOME}/repository/resources/security/wso2carbon.p12 -srcalias wso2carbon-obiam -destalias wso2carbon -srcstorepass wso2carbon -deststorepass wso2carbon \
    && rm -f private-keys.jks\
    && wget -O public-certs.jks "${WSO2_OB_KEYSTORES_URL}/public-certs.jks" \
    && keytool -importkeystore -srckeystore ${USER_HOME}/public-certs.jks -destkeystore ${WSO2_SERVER_HOME}/repository/resources/security/client-truststore.p12 -srcstorepass wso2carbon -deststorepass wso2carbon \
    && rm -r public-certs.jks ; fi

# Adding database connector
RUN if [ -n "$RESOURCE_URL" ]; then wget -O mysql-connector-java-5.1.44.jar "${RESOURCE_URL}/configuration-files/mysql-connector-java-5.1.44.jar"  \
    && cp mysql-connector-java-5.1.44.jar ${WSO2_SERVER_HOME}/repository/components/lib \
    && rm -f mysql-connector-java-5.1.44.jar ; fi

RUN \
    find ${WSO2_SERVER_HOME}/repository/components/dropins -name "org.wso2.financial.services.accelerator.*" -exec rm -rf {} \; \
    && find ${WSO2_SERVER_HOME}/repository/components/lib -name "org.wso2.financial.services.accelerator.*" -exec rm -rf {} \;

RUN \
    cp -r ${WSO2_OB_ACCELERATOR_DIR}/carbon-home/* ${WSO2_SERVER_HOME}/

RUN if [ -n "$RESOURCE_URL" ]; then \
    rm -f ${WSO2_SERVER_HOME}/repository/conf/deployment.toml \
    && wget -O ${WSO2_SERVER_HOME}/repository/conf/deployment.toml \
        "${RESOURCE_URL}/configuration-files/obiam-deployment.toml" ; fi

# add root and issuer certificates to client-truststore.jks
RUN \
    wget -O fs-cert.zip "${OB_TRUSTED_CERTS_URL}" \
    && unzip -d ${USER_HOME} fs-cert.zip  \
    && rm -f fs-cert.zip \

RUN \
    yes | keytool -import -alias fs-root -file root.cer -keystore ${WSO2_SERVER_HOME}/repository/resources/security/client-truststore.p12  -storepass wso2carbon \
    && yes | keytool -import -alias fs-issuer -file issuer.cer -keystore ${WSO2_SERVER_HOME}/repository/resources/security/client-truststore.p12  -storepass wso2carbon \
    && rm -f root.cer issuer.cer

# Copy demo backend WAR from build stage
COPY demo-application/target/ob-demo-backend-1.0.0.war \
     ${WSO2_SERVER_HOME}/repository/deployment/server/webapps/

# change directory rights
RUN \
    chown wso2carbon:wso2 -R ${WSO2_SERVER_HOME}/ \
    && chown wso2carbon:wso2 /etc/motd

# set the non-root user
USER ${USER_ID}

# expose ports
EXPOSE 9446

# initiate container and start WSO2 Carbon server
ENTRYPOINT ["/home/wso2carbon/docker-entrypoint.sh"]