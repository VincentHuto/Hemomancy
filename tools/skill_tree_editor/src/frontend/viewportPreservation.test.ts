import { captureGraphViewport, restoreGraphViewport } from './viewportPreservation';

test('captures and restores graph scroll offsets across a render replacement', () => {
  const before = { scrollLeft: 420, scrollTop: 260 };
  const captured = captureGraphViewport(before);
  const after = { scrollLeft: 0, scrollTop: 0 };

  restoreGraphViewport(after, captured);

  expect(after).toEqual({ scrollLeft: 420, scrollTop: 260 });
});
