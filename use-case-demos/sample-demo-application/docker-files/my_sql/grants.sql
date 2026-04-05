-- Root grants
GRANT ALL PRIVILEGES ON fs_apimgtdb.*      TO 'root'@'%';
GRANT ALL PRIVILEGES ON fs_iskm_configdb.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON fs_userdb.*        TO 'root'@'%';
GRANT ALL PRIVILEGES ON fs_consentdb.*     TO 'root'@'%';
GRANT ALL PRIVILEGES ON fs_identitydb.*    TO 'root'@'%';
GRANT ALL PRIVILEGES ON fs_am_configdb.*   TO 'root'@'%';
GRANT ALL PRIVILEGES ON fs_am_userdb.*     TO 'root'@'%';
FLUSH PRIVILEGES;

-- WSO2 user
CREATE USER IF NOT EXISTS 'wso2'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
GRANT ALL PRIVILEGES ON fs_apimgtdb.*      TO 'wso2'@'%';
GRANT ALL PRIVILEGES ON fs_iskm_configdb.* TO 'wso2'@'%';
GRANT ALL PRIVILEGES ON fs_userdb.*        TO 'wso2'@'%';
GRANT ALL PRIVILEGES ON fs_consentdb.*     TO 'wso2'@'%';
GRANT ALL PRIVILEGES ON fs_identitydb.*    TO 'wso2'@'%';
GRANT ALL PRIVILEGES ON fs_am_configdb.*   TO 'wso2'@'%';
GRANT ALL PRIVILEGES ON fs_am_userdb.*     TO 'wso2'@'%';
FLUSH PRIVILEGES;

-- DELETE FROM fs_apimgtdb.AM_KEY_MANAGER;

