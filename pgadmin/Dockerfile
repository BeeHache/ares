FROM dpage/pgadmin4

COPY servers.json /pgadmin4/servers.json
COPY entrypoint.sh /custom-entrypoint.sh

USER root
RUN chmod +x /custom-entrypoint.sh
USER pgadmin

ENTRYPOINT ["/custom-entrypoint.sh"]
