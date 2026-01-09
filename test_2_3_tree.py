# python
import importlib.util
from pathlib import Path
import pytest

def _load_two_three_tree_module():
    # Load the module from the same directory as this test file
    module_path = Path(__file__).parent / "2_3_tree.py"
    spec = importlib.util.spec_from_file_location("two_three_tree_module", str(module_path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

def test_get_on_empty_tree():
    mod = _load_two_three_tree_module()
    TwoThreeTree = mod.TwoThreeTree

    tree = TwoThreeTree()
    assert tree.get(10) is None
    assert tree.get("nope") is None

def test_put_and_get_single_key():
    mod = _load_two_three_tree_module()
    TwoThreeTree = mod.TwoThreeTree

    tree = TwoThreeTree()
    tree.put(42, "answer")
    assert tree.get(42) == "answer"
    assert tree.get(0) is None

def test_put_multiple_and_get_all_keys():
    mod = _load_two_three_tree_module()
    TwoThreeTree = mod.TwoThreeTree

    tree = TwoThreeTree()
    inserts = [
        (50, "v50"),
        (20, "v20"),
        (70, "v70"),
        (10, "v10"),
        (30, "v30"),
        (60, "v60"),
        (80, "v80"),
        (25, "v25"),
    ]
    for k, v in inserts:
        tree.put(k, v)

    # Verify every inserted key can be retrieved
    for k, v in inserts:
        assert tree.get(k) == v, f"expected key {k} to return {v}"

    # Non-existent key
    assert tree.get(40) is None

def test_put_multiple_and_get_all_keys_v2():
    mod = _load_two_three_tree_module()
    TwoThreeTree = mod.TwoThreeTree

    tree = TwoThreeTree()
    # Insert keys in an order likely to force promotions into internal nodes
    keys = [8, 4, 3, 11, 16, 12, 10, 19, 15, 13]
    for k in keys:
        tree.put(k, f"val{k}")

    # Verify every inserted key can be retrieved
    for k in keys:
        assert tree.get(k) == f"val{k}", f"expected key {k} to return {v}"


def test_get_internal_node_key():
    mod = _load_two_three_tree_module()
    TwoThreeTree = mod.TwoThreeTree

    tree = TwoThreeTree()
    # Insert keys in an order likely to force promotions into internal nodes
    keys = [100, 50, 150, 25, 75, 125, 175, 60, 80, 30]
    for k in keys:
        tree.put(k, f"val{k}")

    # Check some keys that are likely to be in internal nodes after splits
    for k in [50, 100, 150, 75]:
        assert tree.get(k) == f"val{k}"

    # Check leaf keys as well
    for k in [25, 30, 60, 80, 125, 175]:
        assert tree.get(k) == f"val{k}"

    # Missing key
    assert tree.get(999) is None# python
