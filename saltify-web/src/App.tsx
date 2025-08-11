import { Box, Flex, Text, Avatar, HStack } from '@chakra-ui/react';
import { Outlet } from 'react-router';
import logo from './assets/logo.png';

function App() {
  return (
    <Flex direction={'column'} h={'100vh'}>
      {/* App Bar */}
      <Box as={'header'} background={'teal.500'} color={'white'} paddingX={6} paddingY={4} boxShadow={'md'}>
        <HStack>
          <Avatar.Root>
            <Avatar.Fallback name={'Saltify'} />
            <Avatar.Image src={logo} />
          </Avatar.Root>
          <Text fontSize={'xl'} fontWeight={'bold'}>Saltify</Text>
        </HStack>
      </Box>
      <Flex flex={'1'} minH={0}>
        {/* Sidebar */}
        <Box
          as={'nav'}
          width={'15%'}
          minWidth={200}
          h={'full'}
          background={'gray.100'}
          padding={4}
          boxShadow={'md'}
          position={'sticky'}
          top={0}
        >
          {/* TODO: Navi bar */}
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
