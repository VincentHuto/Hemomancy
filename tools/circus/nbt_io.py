"""Typed, round-tripping NBT read/write for structure templates (standard library only).

Every tag is kept as (type id, value) so a file reads and writes back with its tag types unchanged.
"""
import gzip
import struct

END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY = range(13)
_NUM = {BYTE: 'b', SHORT: 'h', INT: 'i', LONG: 'q', FLOAT: 'f', DOUBLE: 'd'}


def load(path):
    data = gzip.open(path).read()
    pos = 0

    def take(fmt):
        nonlocal pos
        value = struct.unpack_from('>' + fmt, data, pos)
        pos += struct.calcsize('>' + fmt)
        return value[0]

    def text():
        nonlocal pos
        n = take('H')
        value = data[pos:pos + n].decode('utf-8')
        pos += n
        return value

    def payload(kind):
        nonlocal pos
        if kind in _NUM:
            return take(_NUM[kind])
        if kind == BYTE_ARRAY:
            n = take('i')
            value = data[pos:pos + n]
            pos += n
            return value
        if kind == STRING:
            return text()
        if kind == LIST:
            inner, n = take('b'), take('i')
            return inner, [payload(inner) for _ in range(n)]
        if kind == COMPOUND:
            out = {}
            while True:
                tag = take('b')
                if tag == END:
                    return out
                name = text()
                out[name] = (tag, payload(tag))
        if kind == INT_ARRAY:
            return [take('i') for _ in range(take('i'))]
        if kind == LONG_ARRAY:
            return [take('q') for _ in range(take('i'))]
        raise ValueError(kind)

    kind = take('b')
    name = text()
    return name, (kind, payload(kind))


def save(path, root):
    name, (kind, value) = root
    out = bytearray()

    def text(s):
        raw = s.encode('utf-8')
        out.extend(struct.pack('>H', len(raw)))
        out.extend(raw)

    def payload(kind, value):
        if kind in _NUM:
            out.extend(struct.pack('>' + _NUM[kind], value))
        elif kind == BYTE_ARRAY:
            out.extend(struct.pack('>i', len(value)))
            out.extend(value)
        elif kind == STRING:
            text(value)
        elif kind == LIST:
            inner, items = value
            out.extend(struct.pack('>bi', inner if items else (inner or END), len(items)))
            for item in items:
                payload(inner, item)
        elif kind == COMPOUND:
            for key, (tag, item) in value.items():
                out.extend(struct.pack('>b', tag))
                text(key)
                payload(tag, item)
            out.append(END)
        elif kind in (INT_ARRAY, LONG_ARRAY):
            out.extend(struct.pack('>i', len(value)))
            for item in value:
                out.extend(struct.pack('>' + ('i' if kind == INT_ARRAY else 'q'), item))
        else:
            raise ValueError(kind)

    out.extend(struct.pack('>b', kind))
    text(name)
    payload(kind, value)
    with gzip.open(path, 'wb') as handle:
        handle.write(bytes(out))


def compound(**tags):
    return COMPOUND, dict(tags)


def string(value):
    return STRING, value
