#!/usr/bin/env python2

import BaseHTTPServer
import urlparse
import cgi
import os
import time
import socket

class MyRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def setup(self):
        self.rfile = socket._fileobject(self.request, "rb", self.rbufsize)
        self.wfile = socket._fileobject(self.request, "wb", self.wbufsize)
#        self.last = 0

    def reply(self, code, reason, data = ""):
        self.send_response(code, reason);
        self.send_header('Content-Length', len(data));
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        print(self.path)
        url_tuple = urlparse.urlsplit(self.path)
        params = dict(cgi.parse_qsl(url_tuple[3]))
        req = url_tuple[2]

#        if self.last != 0 and time.time() - self.last < 0.3:
#            return
#        self.last = time.time()

        if 'action' in params:
            action = params['action']
            if action == "down":
                os.system("xdotool key Next")
            elif action == "up":
                os.system("xdotool key Prior")
            elif action == "test":
                pass
            else:
                self.reply(400, "unknown action")
                return
            self.reply(200, "OK", "OK")
        else:
            self.reply(400, "missing action")

def run(server_class=BaseHTTPServer.HTTPServer,
        handler_class=BaseHTTPServer.BaseHTTPRequestHandler):
    server_address = ('', 5001)
    httpd = server_class(server_address, handler_class)
    httpd.serve_forever()

run(handler_class=MyRequestHandler)
