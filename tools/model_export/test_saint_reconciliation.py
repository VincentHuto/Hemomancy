"""Geometry checks for the reconciled Saint silhouettes, independent of rendering."""
import unittest
import hemorath
import seraphae
import putriciel
import velorum


class SaintReconciliationTest(unittest.TestCase):
    def test_hemorath_iron_grows_from_bone(self):
        rig = hemorath.saint()
        patches = [p for p in rig.parts if 'iron_growth' in p['name']]
        self.assertGreaterEqual(len(patches), 6)
        self.assertTrue(all(p['cubes'] for p in patches))

    def test_seraphae_wings_are_open_nerves(self):
        rig = seraphae.saint()
        nerves = [c for p in rig.parts for c in p['cubes'] if c['material'] == 'nerve']
        self.assertGreater(len(nerves), 30)
        self.assertFalse(any(c['material'] == 'feather' for p in rig.parts for c in p['cubes']))

    def test_putriciel_has_rot_and_growing_colonies(self):
        rig = putriciel.saint()
        self.assertTrue(any('rot_drip' in p['name'] for p in rig.parts))
        self.assertGreaterEqual(len([p for p in rig.parts if 'growth_mushroom' in p['name']]), 4)

    def test_velorum_has_ascetic_hood_and_cord(self):
        rig = velorum.saint()
        names = {p['name'] for p in rig.parts}
        self.assertIn('monastic_hood', names)
        self.assertIn('knotted_cord', names)


if __name__ == '__main__':
    unittest.main()
