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
# STAGE 1: ACCELERATOR SOURCE (obam)
# Uses a pre-built WSO2 Open Banking image that contains the extracted
# accelerator files.
# ----------------------------------------------
# NOTE: The base WSO2 images usually include the full product version (e.g., 4.0.0.0 or 4.0.0)
FROM ${BASE_DOCKER_IMAGE_REGISTRY}/wso2-ob/obam-accelerator:4.0.0.0-alpine AS obam

# Note: The test command verifies the expected accelerator directory exists
RUN test -d /home/wso2carbon/wso2-fsam-accelerator-4.0.0

# ----------------------------------------------
# STAGE 2: FINAL WSO2 AM IMAGE (Merge Accelerator)
# Sets up the base product and integrates the accelerator artifacts.
# ----------------------------------------------
FROM registry.wso2.com/wso2-apim/am:${BASE_PRODUCT_VERSION}.0-alpine

LABEL maintainer="WSO2 Docker Maintainers <dev@wso2.org>" \
      com.wso2.docker.source="https://github.com/wso2/docker-open-banking/releases/tag/v4.0.0.3"

# --- Build Arguments (WSO2 Paths and Names) ---
# build arguments for user/group configurations
ARG USER=wso2carbon
ARG USER_ID=802
ARG USER_GROUP=wso2
ARG USER_GROUP_ID=802
ARG USER_HOME=/home/${USER}
# declaring again BASE_PRODUCT_VERSION ARG to use in build stage
ARG BASE_PRODUCT_VERSION
ARG WSO2_SERVER=wso2am-${BASE_PRODUCT_VERSION}
ARG WSO2_OB_ACCELERATOR=wso2-fsam-accelerator-4.0.0
ARG WSO2_SERVER_HOME=${USER_HOME}/${WSO2_SERVER}
ARG WSO2_OB_ACCELERATOR_DIR=${WSO2_SERVER_HOME}/${WSO2_OB_ACCELERATOR}

# --- Build Arguments (External Resources) ---
ARG OB_TRUSTED_CERTS_URL
ARG WSO2_OB_KEYSTORES_URL
ARG RESOURCE_URL

#ENV ENV=${USER_HOME}"/.ashrc"

# --- Build Arguments (Configuration Overrides) ---
# server hostnames

# --- Setup and Dependencies ---

# Ensure we are root to install tools and manage permissions if needed
USER root

# Install bash temporarily for running the merge script and keytool commands cleanly
# Also install coreutils for 'yes' command compatibility
RUN apk update && apk add bash coreutils openssl

# --- Copy Accelerator ---

# Copy the extracted accelerator folder from the 'obam' stage into the WSO2 APIM home directory
COPY --from=obam ${USER_HOME}/${WSO2_OB_ACCELERATOR} ${WSO2_SERVER_HOME}/${WSO2_OB_ACCELERATOR}

# --- Keystores, Truststores, and JDBC Driver Setup (Needs bash shell) ---

# add keystores to base product
RUN if [ -n "$WSO2_OB_KEYSTORES_URL" ]; then \
    wget -O ${USER_HOME}/private-keys.jks "${WSO2_OB_KEYSTORES_URL}/private-keys.jks" && \
    yes | keytool -importkeystore -srckeystore ${USER_HOME}/private-keys.jks -destkeystore ${WSO2_SERVER_HOME}/repository/resources/security/wso2carbon.jks -srcalias wso2carbon-obam -destalias wso2carbon -srcstorepass wso2carbon -deststorepass wso2carbon && \
    rm -f ${USER_HOME}/private-keys.jks && \
    wget -O ${USER_HOME}/public-certs.jks "${WSO2_OB_KEYSTORES_URL}/public-certs.jks" && \
    yes | keytool -importkeystore -srckeystore ${USER_HOME}/public-certs.jks -destkeystore ${WSO2_SERVER_HOME}/repository/resources/security/client-truststore.jks -srcstorepass wso2carbon -deststorepass wso2carbon && \
    rm -f ${USER_HOME}/public-certs.jks ; fi


# Adding custom error sequence
RUN if [ -n "$RESOURCE_URL" ]; then \
    wget -O ${WSO2_SERVER_HOME}/repository/deployment/server/synapse-configs/default/sequences/customErrorFormatter.xml \
        "${RESOURCE_URL}/configuration-files/customErrorFormatter.xml" ; fi


# Adding database connector
RUN if [ -n "$RESOURCE_URL" ]; then \
    wget -O ${USER_HOME}/mysql-connector-java-5.1.44.jar "${RESOURCE_URL}/configuration-files/mysql-connector-java-5.1.44.jar" && \
    cp ${USER_HOME}/mysql-connector-java-5.1.44.jar ${WSO2_SERVER_HOME}/repository/components/lib && \
    rm -f ${USER_HOME}/mysql-connector-java-5.1.44.jar ; fi

# Remove existing api#fs#backend WAR and deploy new one via wget
RUN rm -rf ${WSO2_SERVER_HOME}/repository/deployment/server/webapps/api#fs#backend \
    && rm -f ${WSO2_SERVER_HOME}/repository/deployment/server/webapps/api#fs#backend.war

# --- Merge and Cleanup ---

# copy the WSO2 OB APIM accelerator artifacts on top of WSO2 APIM base product
# remove old open banking artifacts from base product (using bash's 'find' for better compatibility)
RUN set -ex; \
    find ${WSO2_SERVER_HOME}/repository/components/dropins -name "org.wso2.financial.services.accelerator.*" -exec rm -rf {} \; && \
    find ${WSO2_SERVER_HOME}/repository/components/lib -name "org.wso2.financial.services.accelerator.*" -exec rm -rf {} \;

# copy open banking artifacts using cp -r (this is effectively the 'merge' operation)
RUN cp -r ${WSO2_OB_ACCELERATOR_DIR}/carbon-home/* ${WSO2_SERVER_HOME}/

RUN if [ -n "$RESOURCE_URL" ]; then \
    rm -f ${WSO2_SERVER_HOME}/repository/conf/deployment.toml \
    && wget -O ${WSO2_SERVER_HOME}/repository/conf/deployment.toml \
        "${RESOURCE_URL}/configuration-files/obam-deployment.toml" ; fi

ARG SEQUENCE_FILE=${WSO2_SERVER_HOME}/repository/deployment/server/synapse-configs/default/sequences/_cors_request_handler_.xml
RUN \
  sed -i -e 's|</sequence>|<sequence key="customErrorFormatter"/></sequence>|g' ${SEQUENCE_FILE}

# add root and issuer certificates to client-truststore.jks
RUN \
    wget -O ${USER_HOME}/fs-cert.zip "${OB_TRUSTED_CERTS_URL}" && \
    unzip -d ${USER_HOME} ${USER_HOME}/fs-cert.zip && \
    rm -f ${USER_HOME}/fs-cert.zip

RUN \
    yes | keytool -import -alias fs-root -file ${USER_HOME}/root.cer -keystore ${WSO2_SERVER_HOME}/repository/resources/security/client-truststore.jks  -storepass wso2carbon && \
    yes | keytool -import -alias fs-issuer -file ${USER_HOME}/issuer.cer -keystore ${WSO2_SERVER_HOME}/repository/resources/security/client-truststore.jks  -storepass wso2carbon && \
    rm -f ${USER_HOME}/root.cer ${USER_HOME}/issuer.cer

# change directory rights
RUN \
    chown wso2carbon:wso2 -R ${WSO2_SERVER_HOME}/ \
    && chown wso2carbon:wso2 /etc/motd


# set the non-root user
USER ${USER}

# expose ports
EXPOSE 9443 8280 8243

RUN test -d /home/wso2carbon/wso2am-4.5.0

# Fix IS service URL to point to obiam instead of obam
RUN sed -i 's|https://obam:9446/services/|https://obiam:9446/services/|g' /home/wso2carbon/wso2am-4.5.0/repository/conf/deployment.toml

ENTRYPOINT ["/home/wso2carbon/docker-entrypoint.sh"]