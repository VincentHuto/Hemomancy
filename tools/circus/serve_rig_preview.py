"""Serve local authoring previews and save their canvas comparisons as PNGs."""
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
import base64
import json


def serve(output, prefix, port):
    class PreviewHandler(SimpleHTTPRequestHandler):
        def do_POST(self):
            if self.path != '/snapshot':
                self.send_error(404)
                return
            length = int(self.headers.get('Content-Length', 0))
            if not 0 < length <= 2 * 1024 * 1024:
                self.send_error(413)
                return
            data = json.loads(self.rfile.read(length))
            state = data.get('state')
            if state not in ('idle', 'slither', 'breath'):
                self.send_error(400)
                return
            png = base64.b64decode(data['image'].split(',', 1)[1], validate=True)
            if not png.startswith(b'\x89PNG\r\n\x1a\n'):
                self.send_error(400)
                return
            name = prefix + '-' + state + '.png'
            (output / name).write_bytes(png)
            self.send_response(200)
            self.send_header('Content-Type', 'text/plain')
            self.end_headers()
            self.wfile.write(name.encode('ascii'))

    print(f'Preview: http://127.0.0.1:{port}/preview.html', flush=True)
    ThreadingHTTPServer(('127.0.0.1', port), partial(PreviewHandler, directory=str(output))).serve_forever()
