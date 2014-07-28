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
 * Reimplementation of the java.swing.TreeNode interface with fewer methods
 *  
 * @author Andreas Schwier (info@cardcontact.de)
 */
public interface TreeNode {
    
    /**
     * Return child at position childIndex
     * 
     * @param childIndex
     * @return Child node
     */
    public TreeNode getChildAt(int childIndex);
    
    
    
    /**
     * Return number of childs
     *  
     * @return Number of childs
     */
    public int getChildCount();

    
    
    /**
     * Get index for child
     * 
     * @param child to look for
     * @return Child index or -1
     */
    public int getIndex(TreeNode child);
    
    
    
    /**
     * Get parent for child
     * 
     * @return Parent or null if root or unknown
     */
    public TreeNode getParent();

    
    
    /**
     * Return true, if node is a leaf node
     * 
     * @return Node is leaf node
     */
    public boolean isLeaf();
}
