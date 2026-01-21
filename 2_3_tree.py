# ...existing code...
class Node:
    def __init__(self, keys=None, vals=None, children=None):
        self.keys = list(keys) if keys else []
        self.vals = list(vals) if vals else []
        self.children = list(children) if children else []

    def is_leaf(self):
        return len(self.children) == 0

    def __repr__(self):
        return f"Node(keys={self.keys})"


class TwoThreeTree:
    def __init__(self):
        self.root = None

    def get(self, key):
        return self._get(self.root, key)

    def _get(self, node, key):
        if node is None:
            return None
        # scan through keys
        for i, k in enumerate(node.keys):
            if key == k:
                return node.vals[i]
            if key < k:
                return self._get(node.children[i] if not node.is_leaf() else None, key)
        return self._get(node.children[-1] if not node.is_leaf() else None, key)