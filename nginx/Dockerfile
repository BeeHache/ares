FROM nginx:alpine

# The default entrypoint for nginx:alpine will look for templates in /etc/nginx/templates
# and run envsubst on them, outputting to /etc/nginx/conf.d/ (usually)
# To overwrite the main nginx.conf, we can use this location:
COPY nginx.conf.template /etc/nginx/templates/nginx.conf.template

# We also need to set the environment variable for where the output goes
ENV NGINX_ENVSUBST_OUTPUT_DIR=/etc/nginx
