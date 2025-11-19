/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

/**
 *
 * @author DEI-ESINF
 * @param <E>
 */
public class AVL <E extends Comparable<E>> extends BST<E> {


    private int nodeHeight(Node<E> node) {
        return node == null ? -1 : node.getHeight();
    }

    private void updateHeight(Node<E> node) {
        if (node == null) return;
        int hl = nodeHeight(node.getLeft());
        int hr = nodeHeight(node.getRight());
        node.setHeight(1 + Math.max(hl, hr));
    }

    private int balanceFactor(Node<E> node){
        if (node == null) return 0; // por convenção, FB de null é 0
        int hl = node.getLeft()  == null ? -1 : nodeHeight(node.getLeft());
        int hr = node.getRight() == null ? -1 : nodeHeight(node.getRight());
        return hl - hr;
    }

    private Node<E> rightRotation(Node<E> node){
        Node<E> leftson = node.getLeft();
        node.setLeft(leftson.getRight());
        leftson.setRight(node);
        updateHeight(node);
        updateHeight(leftson);
        return leftson;
    }

    private Node<E> leftRotation(Node<E> node){
        Node<E> rightson = node.getRight();
        node.setRight(rightson.getLeft());
        rightson.setLeft(node);
        updateHeight(node);
        updateHeight(rightson);
        return rightson;
    }

    private Node<E> twoRotations(Node<E> node){
        if (balanceFactor(node) < 0) {
            node.setLeft(leftRotation(node.getLeft()));
            node = rightRotation(node);}
        else {
            node.setRight(rightRotation(node.getRight()));
            node = leftRotation(node);}
        return node;
    }

    private Node<E> balanceNode(Node<E> node){
        if (node == null) return null;

        int bf = balanceFactor(node);

        if (bf > 1) {
            if (balanceFactor(node.getLeft()) < 0) {
                node.setLeft(leftRotation(node.getLeft()));
                updateHeight(node.getLeft());
            }
            node = rightRotation(node);
            updateHeight(node.getRight());
            updateHeight(node);
            return node;
        }

        if (bf < -1) {
            if (balanceFactor(node.getRight()) > 0) {
                node.setRight(rightRotation(node.getRight()));
                updateHeight(node.getRight());
            }
            node = leftRotation(node);
            updateHeight(node.getLeft());
            updateHeight(node);
            return node;
        }

        updateHeight(node);
        return node;
    }

    @Override
    public void insert(E element){
        root = insert(element, root);
    }

    private Node<E> insert(E element, Node<E> node){
        if (node == null) return new Node<>(element, null, null);

        int cmp = element.compareTo(node.getElement());
        if (cmp < 0)
            node.setLeft(insert(element, node.getLeft()));
        else if (cmp > 0)
            node.setRight(insert(element, node.getRight()));
        else
            node.setElement(element); // substitui

        return balanceNode(node); // equilibrar ao subir
    }

    @Override
    public void remove(E element){
        root = remove(element, root());
    }

    private Node<E> remove(E element, Node<E> node) {
        if (node == null) {
            return null;
        }
        if (element.compareTo(node.getElement())==0) { // node is the Node to be removed

            if (node.getLeft() == null && node.getRight() == null) { //node is a leaf (has no childs)
                return null;
            }
            if (node.getLeft() == null) {   //has only right child
                return node.getRight();
            }
            if (node.getRight() == null) {  //has only left child
                return node.getLeft();
            }
            //has two child trees
            //replace the elem with the smallest of right subtree and remove it
            E smallElem = smallestElement(node.getRight());
            node.setElement(smallElem);
            node.setRight( remove(smallElem, node.getRight()) );
            node = balanceNode(node);
        }
        else if (element.compareTo(node.getElement()) < 0) {
            node.setLeft( remove(element, node.getLeft()) );
            node = balanceNode(node);
        }
        else {
            node.setRight( remove(element, node.getRight()) );
            node = balanceNode(node);
        }
        return node;
    }


    public boolean equals(Object otherObj) {

        if (this == otherObj)
            return true;

        if (otherObj == null || this.getClass() != otherObj.getClass())
            return false;

        AVL<E> second = (AVL<E>) otherObj;
        return equals(root, second.root);
    }

    public boolean equals(Node<E> root1, Node<E> root2) {
        if (root1 == null && root2 == null)
            return true;
        else if (root1 != null && root2 != null) {
            if (root1.getElement().compareTo(root2.getElement()) == 0) {
                return equals(root1.getLeft(), root2.getLeft())
                        && equals(root1.getRight(), root2.getRight());
            } else
                return false;
        }
        else return false;
    }

}
