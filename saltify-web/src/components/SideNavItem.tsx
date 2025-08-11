/*
 * This file includes code from an MIT-licensed project:
 * Project Name: Chakra UI Docs Side Navigation Item
 * Source: https://github.com/chakra-ui/chakra-ui
 * Licensed under the MIT License.
 *
 * Copyright (c) 2019 Segun Adebayo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Modifications:
 * - Modified by Young, 2025
 */

import { HStack, Link } from '@chakra-ui/react';
import { useLocation, useNavigate } from 'react-router';

function SideNavItem(props: { href: string; title: string }) {
  const location = useLocation();
  const navigate = useNavigate();
  return (
    <HStack
      py={1.5}
      ps={4}
      pe={3}
      rounded={'sm'}
      color={'fg.muted'}
      _hover={{
        layerStyle: 'fill.subtle',
      }}
      _currentPage={{
        colorPalette: 'teal',
        fontWeight: 'medium',
        layerStyle: 'fill.subtle',
      }}
      asChild
    >
      <Link
        aria-current={location.pathname === props.href ? 'page' : undefined}
        _hover={{ textDecoration: 'none' }}
        onClick={() => navigate(props.href)}
      >
        {props.title}
      </Link>
    </HStack>
  );
}

export default SideNavItem;