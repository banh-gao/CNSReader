/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2006 CardContact Software & System Consulting
 * |'##> <##'|  Andreas Schwier, 32429 Minden, Germany (www.cardcontact.de)
 *  --------- 
 *
 *  This file is part of OpenSCDP.
 *
 *  OpenSCDP is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  OpenSCDP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSCDP; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.cardcontact.tlv;


/**
 * Reimplementation of java.swing.MutableTreeNode removing some signature and inefficencies
 *  
 * @author Andreas Schwier (info@cardcontact.de)
 */
public interface MutableTreeNode extends TreeNode {
    /**
     * Insert a tree node into a mutable tree node at given index
     * 
     * @param child Child to insert
     * @param index Index at which to insert the child
     */
    public void insert(TreeNode child, int index);
    
    
    
    /**
     * Remove child from mutable tree node
     * 
     * @param index Index of child to be removed
     */
    public void remove(int index);
    
    
    
    /**
     * Set the parent for a mutable tree node
     * 
     * @param parent
     */
    public void setParent(MutableTreeNode parent);
}
