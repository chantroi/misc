#!/usr/bin/bash

python -m gunicorn --reload -b 0.0.0.0:10808 main:wsgi_app
#python -m hypercorn main:asgi_app --bind '0.0.0.0:10808' --reload