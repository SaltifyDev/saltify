import { Box, Flex, Text, Avatar, HStack, VStack } from '@chakra-ui/react';
import { Outlet } from 'react-router';
import logo from './assets/logo.png';
import SideNavItem from './components/SideNavItem.tsx';

interface NavItem {
  href: string;
  title: string;
}

const navItems: NavItem[] = [{ href: '/', title: '首页' }];

function App() {
  return (
    <Flex direction={'column'} h={'100vh'}>
      {/* App Bar */}
      <Box as={'header'} bg={'teal.500'} color={'white'} px={6} py={4} boxShadow={'md'}>
        <HStack>
          <Avatar.Root>
            <Avatar.Fallback name={'Saltify'} />
            <Avatar.Image src={logo} />
          </Avatar.Root>
          <Text fontSize={'xl'} fontWeight={'bold'}>
            Saltify
          </Text>
        </HStack>
      </Box>
      <Flex flex={'1'} minH={0}>
        {/* Sidebar */}
        <Box
          as={'nav'}
          w={'15%'}
          h={'full'}
          minW={200}
          bg={'gray.50'}
          p={4}
          boxShadow={'md'}
          position={'sticky'}
          top={0}
        >
          <VStack align={'stretch'}>
            {navItems.map((item) => (
              <SideNavItem key={item.href} href={item.href} title={item.title} />
            ))}
          </VStack>
        </Box>
        {/* Main Content */}
        <Box as={'main'} flex={'1'} padding={6} overflowY={'auto'} background={'white'}>
          <Outlet />
        </Box>
      </Flex>
    </Flex>
  );
}

export default App;
