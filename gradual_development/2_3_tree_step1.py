import importlib.util
import os

# Load the module from the repo root
repo_root = os.path.dirname(os.path.abspath(__file__))
module_path = os.path.join(repo_root, "..", "2_3_tree.py")
spec = importlib.util.spec_from_file_location("two_three_tree", module_path)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
TwoThreeTree = module.TwoThreeTree


def test_get_on_empty_tree():
    t = TwoThreeTree()
    assert t.get(1) is None
    