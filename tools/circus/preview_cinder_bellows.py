"""Make a portable textured rig preview under build/cinder-bellows-rework.

Run after build_cinder_bellows.py; use --serve for the interactive preview and PNG saving.
The preview uses the actual Blockbench geometry/atlas and mirrors Java setupPose;
it is an authoring preview, not a Minecraft client acceptance check.
"""
from pathlib import Path
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
import base64
import json
import sys
import urllib.request
from build_cinder_bellows import MODEL, ROOT


def main():
    output = ROOT / 'build/cinder-bellows-rework'
    output.mkdir(parents=True, exist_ok=True)
    module = output / 'three.module.js'
    if not module.exists():
        urllib.request.urlretrieve('https://cdn.jsdelivr.net/npm/three@0.169.0/build/three.module.js', module)
    models = {'after': json.loads(MODEL.read_text(encoding='utf-8'))}
    before = output / 'before/CinderBellowsModel.bbmodel'
    if before.exists():
        models['before'] = json.loads(before.read_text(encoding='utf-8'))
    template = Path(__file__).with_name('cinder_bellows_preview.html').read_text(encoding='utf-8')
    (output / 'preview.html').write_text(template.replace('/* MODEL_DATA */', json.dumps(models)), encoding='utf-8')
    print(output / 'preview.html')
    if '--serve' in sys.argv:
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
                name = 'cinder-bellows-' + state + '.png'
                (output / name).write_bytes(png)
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain')
                self.end_headers()
                self.wfile.write(name.encode('ascii'))

        print('Preview: http://127.0.0.1:8766/preview.html', flush=True)
        ThreadingHTTPServer(('127.0.0.1', 8766), partial(PreviewHandler, directory=str(output))).serve_forever()


if __name__ == '__main__':
    main()
