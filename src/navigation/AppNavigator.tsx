import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createStackNavigator } from '@react-navigation/stack';
import { NavigationContainer } from '@react-navigation/native';
import HomeScreen from '../screens/HomeScreen';
import StatisticsScreen from '../screens/StatisticsScreen';
import SettingsScreen from '../screens/SettingsScreen';
import BlockingScreen from '../screens/BlockingScreen';
import { Home, BarChart2, Settings } from 'lucide-react-native';

const Tab = createBottomTabNavigator();
const Stack = createStackNavigator();

import { useAppTheme } from '../theme';

const TabNavigator = () => {
    const theme = useAppTheme();

    return (
        <Tab.Navigator
                screenOptions={{
                    headerShown: false,
                    tabBarActiveTintColor: theme.primary,
                    tabBarInactiveTintColor: theme.textSecondary,
                    tabBarStyle: {
                        borderTopColor: theme.tabBarBorder,
                        backgroundColor: theme.tabBar,
                        paddingBottom: 8,
                        paddingTop: 8,
                        height: 60,
                        borderTopWidth: 1,
                        elevation: 0, // Remove shadow on Android for cleaner look
                    },
                }}
            >
                <Tab.Screen
                    name="Home"
                    component={HomeScreen}
                    options={{
                        tabBarIcon: ({ color, size }) => <Home color={color} size={size} />,
                    }}
                />
                <Tab.Screen
                    name="Statistics"
                    component={StatisticsScreen}
                    options={{
                        tabBarIcon: ({ color, size }) => <BarChart2 color={color} size={size} />,
                    }}
                />
                <Tab.Screen
                    name="Settings"
                    component={SettingsScreen}
                    options={{
                        tabBarIcon: ({ color, size }) => <Settings color={color} size={size} />,
                    }}
                />
            </Tab.Navigator>
    );
};

const AppNavigator = () => {
    return (
        <NavigationContainer>
            <Stack.Navigator screenOptions={{ headerShown: false }}>
                <Stack.Screen name="Main" component={TabNavigator} />
                <Stack.Screen 
                    name="Blocking" 
                    component={BlockingScreen}
                    options={{
                        presentation: 'modal',
                        gestureEnabled: true,
                    }}
                />
            </Stack.Navigator>
        </NavigationContainer>
    );
};

export default AppNavigator;
