package com.fly.algorithm.tree;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * 定义树的节点
 * 
 * @author fly
 */
public class TNode
{
    // 树节点保存数据
    private String data;

    // 树左节点指针
    private TNode lnode;

    // 树右节点指针
    private TNode rnode;

    public TNode()
    {

    }

    public TNode(String data)
    {
        this.data = data;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public TNode getLnode()
    {
        return lnode;
    }

    public void setLnode(TNode lnode)
    {
        this.lnode = lnode;
    }

    public TNode getRnode()
    {
        return rnode;
    }

    public void setRnode(TNode rnode)
    {
        this.rnode = rnode;
    }

    public static TNode createTree(TNode root)
        throws IOException
    {
        BufferedReader strin = new BufferedReader(new InputStreamReader(System.in));
        String str = strin.readLine();
        if ("#".equals(str))
        {
            root = null;
        }
        else
        {
            root = new TNode();
            root.data = str;
            System.out.format("请输入%s的左孩子:", str);
            createTree(root.lnode);
            System.out.format("请输入%s的右孩子:", str);
            createTree(root.rnode);
        }
        return null;
    }

    public static void main(String args[])
        throws IOException
    {
        TNode root = new TNode("root");
        System.out.format("请输入根节点:");
        createTree(root);
    }
}
