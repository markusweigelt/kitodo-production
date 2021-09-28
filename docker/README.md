# Docker

# Table of contents
1. [Prerequisites](#Prerequisites)
2. [Some paragraph](#paragraph1)
    1. [Sub paragraph](#subparagraph1)
3. [Another paragraph](#paragraph2)

## Prerequisites

### Install Docker Engine
https://docs.docker.com/get-docker/

### Install Docker Compose
https://docs.docker.com/compose/install/

## Latest version

Start the container for the current project via the CLI in this folder.

```
docker-compose up -d
```

Data of the container volumes are stored under ./data/{PROJEKTNAME}/services/.


## Legacy versions

For example, older project versions contain an older version of Elasticsearch.

```
docker-compose --env-file=.env.kitodo-3.3 up -d
```

## Containers

### Elasticsearch 

Ports: 9200, 9300

### MySQL

Ports: 3306

### OpenLDAP

Ports: 389, 636

### phpLDAPadmin

Ports: 6080


## Configure LDAP with phpLDAPadmin

### Import Testusers with bootstrap.ldif


